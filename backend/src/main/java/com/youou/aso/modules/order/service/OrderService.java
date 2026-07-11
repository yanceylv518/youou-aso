package com.youou.aso.modules.order.service;

import com.youou.aso.common.api.PageResult;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.common.util.BusinessNumberGenerator;
import com.youou.aso.modules.account.domain.CustomerAccount;
import com.youou.aso.modules.account.repository.CustomerAccountRepository;
import com.youou.aso.modules.appmanagement.domain.CustomerApp;
import com.youou.aso.modules.appmanagement.domain.CustomerAppStatus;
import com.youou.aso.modules.appmanagement.domain.MarketRegion;
import com.youou.aso.modules.appmanagement.repository.CustomerAppRepository;
import com.youou.aso.modules.appmanagement.repository.MarketRegionRepository;
import com.youou.aso.modules.order.domain.AsoOrder;
import com.youou.aso.modules.order.domain.OrderCommentDetail;
import com.youou.aso.modules.order.domain.OrderItem;
import com.youou.aso.modules.order.domain.OrderStatus;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.order.dto.CreateOrderCommand;
import com.youou.aso.modules.order.dto.OrderQuery;
import com.youou.aso.modules.order.repository.OrderCommentDetailRepository;
import com.youou.aso.modules.order.repository.OrderItemRepository;
import com.youou.aso.modules.order.repository.OrderRepository;
import com.youou.aso.modules.pricing.domain.PriceCode;
import com.youou.aso.modules.pricing.domain.PricingConfig;
import com.youou.aso.modules.pricing.repository.PricingConfigRepository;
import com.youou.aso.modules.wallet.service.WalletDebitResult;
import com.youou.aso.modules.wallet.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final Set<Integer> ALLOWED_EXECUTION_HOURS = Set.of(1, 2, 4, 6, 8, 12, 16, 20, 24);

    private final CustomerAppRepository customerAppRepository;
    private final MarketRegionRepository marketRegionRepository;
    private final PricingConfigRepository pricingConfigRepository;
    private final CustomerAccountRepository customerAccountRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderCommentDetailRepository orderCommentDetailRepository;
    private final WalletService walletService;
    private final OrderNotificationSender orderNotificationSender;
    private final Clock clock;

    public OrderService(
            CustomerAppRepository customerAppRepository,
            MarketRegionRepository marketRegionRepository,
            PricingConfigRepository pricingConfigRepository,
            CustomerAccountRepository customerAccountRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderCommentDetailRepository orderCommentDetailRepository,
            WalletService walletService,
            OrderNotificationSender orderNotificationSender,
            Clock clock
    ) {
        this.customerAppRepository = customerAppRepository;
        this.marketRegionRepository = marketRegionRepository;
        this.pricingConfigRepository = pricingConfigRepository;
        this.customerAccountRepository = customerAccountRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderCommentDetailRepository = orderCommentDetailRepository;
        this.walletService = walletService;
        this.orderNotificationSender = orderNotificationSender;
        this.clock = clock;
    }

    @Transactional
    public AsoOrder createCustomerOrder(Long customerId, CreateOrderCommand command) {
        validateCreateCommand(customerId, command);
        CustomerApp app = loadActiveCustomerApp(customerId, command.customerAppId());
        PriceSnapshot priceSnapshot = calculatePrice(command, app);
        AsoOrder order = buildOrder(customerId, app, command, priceSnapshot);
        order.setOrderNo(generateOrderNo());
        applyPaymentStatus(order, priceSnapshot, "ORDER_DEDUCT:" + command.orderType().name());
        AsoOrder saved = orderRepository.save(order);
        orderItemRepository.saveAll(saved.getId(), priceSnapshot.items());
        orderCommentDetailRepository.saveAll(saved.getId(), priceSnapshot.commentDetails());
        saved.setItems(priceSnapshot.items());
        saved.setCommentDetails(priceSnapshot.commentDetails());
        notifyOrderCreated(saved);
        return saved;
    }

    @Transactional
    public AsoOrder resubmitPendingPaymentOrder(Long customerId, Long orderId, CreateOrderCommand command) {
        validateCreateCommand(customerId, command);
        if (orderId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        AsoOrder order = orderRepository.findById(orderId)
                .filter(candidate -> customerId.equals(candidate.getCustomerId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!OrderStatus.PENDING_PAYMENT.equals(order.getStatus()) || order.getSourceAuditId() != null) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        CustomerApp app = loadActiveCustomerApp(customerId, command.customerAppId());
        PriceSnapshot priceSnapshot = calculatePrice(command, app);
        copyOrderDraft(order, app, command, priceSnapshot);
        applyPaymentStatus(order, priceSnapshot, "ORDER_DEDUCT_RETRY:" + order.getOrderNo());
        AsoOrder updated = orderRepository.updatePaymentDraft(order);
        orderItemRepository.deleteByOrderId(updated.getId());
        orderCommentDetailRepository.deleteByOrderId(updated.getId());
        orderItemRepository.saveAll(updated.getId(), priceSnapshot.items());
        orderCommentDetailRepository.saveAll(updated.getId(), priceSnapshot.commentDetails());
        updated.setItems(priceSnapshot.items());
        updated.setCommentDetails(priceSnapshot.commentDetails());
        notifyOrderCreated(updated);
        return updated;
    }

    @Transactional
    public AsoOrder confirmOrder(Long orderId, Long adminId) {
        AsoOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!OrderStatus.PENDING_CONFIRM.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        order.setStatus(OrderStatus.PENDING_EXECUTION);
        order.setConfirmedByAdminId(adminId);
        order.setConfirmedAt(now());
        return orderRepository.update(order);
    }

    @Transactional
    public List<AsoOrder> confirmOrders(List<Long> orderIds, Long adminId) {
        if (orderIds == null || orderIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        List<AsoOrder> orders = orderIds.stream()
                .distinct()
                .map(orderId -> orderRepository.findById(orderId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND)))
                .toList();
        if (orders.stream().anyMatch(order -> !OrderStatus.PENDING_CONFIRM.equals(order.getStatus()))) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        LocalDateTime confirmedAt = now();
        return orders.stream()
                .map(order -> {
                    order.setStatus(OrderStatus.PENDING_EXECUTION);
                    order.setConfirmedByAdminId(adminId);
                    order.setConfirmedAt(confirmedAt);
                    return orderRepository.update(order);
                })
                .toList();
    }

    @Transactional
    public List<AsoOrder> listCustomerOrders(Long customerId, OrderQuery query) {
        if (customerId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        completeExpiredExecutingOrders();
        return attachDetails(attachItems(orderRepository.findByCustomerId(customerId, query)));
    }

    @Transactional
    public PageResult<AsoOrder> pageCustomerOrders(Long customerId, OrderQuery query, Integer page, Integer pageSize) {
        if (customerId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        Page pageInfo = normalizePage(page, pageSize);
        completeExpiredExecutingOrders();
        long total = orderRepository.countByCustomerId(customerId, query);
        List<AsoOrder> items = total == 0
                ? List.of()
                : attachDetails(attachItems(orderRepository.findByCustomerId(customerId, query, pageInfo.pageSize(), pageInfo.offset())));
        return new PageResult<>(items, pageInfo.page(), pageInfo.pageSize(), total);
    }

    @Transactional
    public List<AsoOrder> listAdminOrders(OrderQuery query) {
        completeExpiredExecutingOrders();
        return attachCustomers(attachDetails(attachItems(orderRepository.findAll(query))));
    }

    @Transactional
    public PageResult<AsoOrder> pageAdminOrders(OrderQuery query, Integer page, Integer pageSize) {
        Page pageInfo = normalizePage(page, pageSize);
        completeExpiredExecutingOrders();
        long total = orderRepository.countAll(query);
        List<AsoOrder> items = total == 0
                ? List.of()
                : attachDetails(attachItems(orderRepository.findAll(query, pageInfo.pageSize(), pageInfo.offset())));
        attachCustomers(items);
        return new PageResult<>(items, pageInfo.page(), pageInfo.pageSize(), total);
    }

    @Transactional
    public AsoOrder getCustomerOrder(Long customerId, Long orderId) {
        if (customerId == null || orderId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        completeExpiredExecutingOrders();
        AsoOrder order = orderRepository.findById(orderId)
                .filter(candidate -> customerId.equals(candidate.getCustomerId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return attachDetails(attachItems(order));
    }

    @Transactional
    public AsoOrder getAdminOrder(Long orderId) {
        if (orderId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        completeExpiredExecutingOrders();
        AsoOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return attachCustomer(attachDetails(attachItems(order)));
    }

    @Transactional
    public AsoOrder executeOrder(Long orderId, Long adminId) {
        AsoOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        applyExecution(order, adminId, now());
        return orderRepository.update(order);
    }

    @Transactional
    public List<AsoOrder> executeOrders(List<Long> orderIds, Long adminId) {
        if (orderIds == null || orderIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        List<AsoOrder> orders = orderIds.stream()
                .distinct()
                .map(orderId -> orderRepository.findById(orderId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND)))
                .toList();
        if (orders.stream().anyMatch(order -> !OrderStatus.PENDING_EXECUTION.equals(order.getStatus()))) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        LocalDateTime now = now();
        return orders.stream()
                .map(order -> {
                    applyExecution(order, adminId, now);
                    return orderRepository.update(order);
                })
                .toList();
    }

    @Transactional
    public AsoOrder pauseOrder(Long orderId, Long adminId) {
        AsoOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!OrderStatus.EXECUTING.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        order.setStatus(OrderStatus.PAUSED);
        return orderRepository.update(order);
    }

    @Transactional
    public List<AsoOrder> pauseOrders(List<Long> orderIds, Long adminId) {
        if (orderIds == null || orderIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        List<AsoOrder> orders = orderIds.stream()
                .distinct()
                .map(orderId -> orderRepository.findById(orderId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND)))
                .toList();
        if (orders.stream().anyMatch(order -> !OrderStatus.EXECUTING.equals(order.getStatus()))) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        return orders.stream()
                .map(order -> {
                    order.setStatus(OrderStatus.PAUSED);
                    return orderRepository.update(order);
                })
                .toList();
    }

    @Transactional
    public AsoOrder resumeOrder(Long orderId, Long adminId) {
        AsoOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!OrderStatus.PAUSED.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        order.setStatus(OrderStatus.EXECUTING);
        return orderRepository.update(order);
    }

    @Transactional
    public AsoOrder cancelOrder(Long orderId, Long adminId, String reason) {
        AsoOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!OrderStatus.PENDING_CONFIRM.equals(order.getStatus())
                && !OrderStatus.PENDING_EXECUTION.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        if (order.getRefundTransactionId() != null) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_REFUNDED);
        }

        if (order.getDeductedTransactionId() != null && order.getTotalAmount() != null) {
            WalletDebitResult refund = walletService.refundForOrder(
                    order.getCustomerId(),
                    order.getTotalAmount(),
                    "ORDER_REFUND:" + order.getOrderNo()
            );
            order.setRefundTransactionId(refund.transactionId());
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setRejectReason(normalizeCancelReason(reason));
        return orderRepository.update(order);
    }

    private void validateCreateCommand(Long customerId, CreateOrderCommand command) {
        if (customerId == null || command == null || command.customerAppId() == null || command.orderType() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        if (command.executionHours() != null && !ALLOWED_EXECUTION_HOURS.contains(command.executionHours())) {
            throw new BusinessException(ErrorCode.EXECUTION_HOURS_INVALID);
        }
        if (command.startDate() == null || command.endDate() == null || command.endDate().isBefore(command.startDate())) {
            throw new BusinessException(ErrorCode.ORDER_DATE_INVALID);
        }
    }

    private CustomerApp loadActiveCustomerApp(Long customerId, Long customerAppId) {
        return customerAppRepository.findById(customerAppId)
                .filter(candidate -> customerId.equals(candidate.getCustomerId()))
                .filter(candidate -> CustomerAppStatus.ACTIVE.equals(candidate.getStatus()))
                .orElseThrow(() -> new BusinessException(ErrorCode.APP_NOT_VERIFIED));
    }

    private AsoOrder buildOrder(Long customerId, CustomerApp app, CreateOrderCommand command, PriceSnapshot priceSnapshot) {
        AsoOrder order = new AsoOrder();
        order.setCustomerId(customerId);
        copyOrderDraft(order, app, command, priceSnapshot);
        return order;
    }

    private void copyOrderDraft(AsoOrder order, CustomerApp app, CreateOrderCommand command, PriceSnapshot priceSnapshot) {
        String regionCode = priceSnapshot.orderRegionCode() == null
                ? resolveLinkedRegion(app, command.regionCode())
                : priceSnapshot.orderRegionCode();
        order.setCustomerAppId(app.getId());
        order.setOrderType(command.orderType());
        order.setPricingCode(priceSnapshot.primaryPriceCode());
        order.setStoreType(app.getStoreType());
        order.setRegionCode(regionCode);
        order.setAppIdentifier(app.getAppIdentifier());
        order.setAppName(app.getAppName());
        order.setAppIconUrl(app.getAppIconUrl());
        order.setOrderStartDate(command.startDate());
        order.setOrderEndDate(command.endDate());
        order.setExecutionHours(command.executionHours());
        order.setTotalDays(totalDays(command.startDate(), command.endDate()));
        order.setQuantity(priceSnapshot.quantity());
        order.setUnitPrice(priceSnapshot.unitPrice());
        order.setTotalAmount(priceSnapshot.totalAmount());
        order.setExpectedCompletedAt(command.endDate().plusDays(1).atStartOfDay());
    }

    private void applyPaymentStatus(AsoOrder order, PriceSnapshot priceSnapshot, String remark) {
        try {
            WalletDebitResult debit = walletService.debitForOrder(
                    order.getCustomerId(),
                    priceSnapshot.totalAmount(),
                    remark
            );
            order.setStatus(OrderStatus.PENDING_CONFIRM);
            order.setBalanceBefore(debit.balanceBefore());
            order.setBalanceAfter(debit.balanceAfter());
            order.setDeductedTransactionId(debit.transactionId());
        } catch (BusinessException exception) {
            if (!ErrorCode.BALANCE_NOT_ENOUGH.equals(exception.getErrorCode())) {
                throw exception;
            }
            BigDecimal currentBalance = walletService.currentBalanceForUpdate(order.getCustomerId());
            order.setStatus(OrderStatus.PENDING_PAYMENT);
            order.setBalanceBefore(currentBalance);
            order.setBalanceAfter(currentBalance);
            order.setDeductedTransactionId(null);
        }
    }

    private PriceSnapshot calculatePrice(CreateOrderCommand command, CustomerApp app) {
        return switch (command.orderType()) {
            case KEYWORD_INSTALL -> calculateKeywordInstall(command, app);
            case DOWNLOAD -> calculateDownload(command, app);
            case RATING -> calculateRating(command, app);
            case REVIEW -> calculateReview(command, app);
            case RANK_GUARANTEE, KEYWORD_COVERAGE -> throw new BusinessException(ErrorCode.SPECIAL_AUDIT_NOT_APPROVED);
        };
    }

    private String resolveLinkedRegion(CustomerApp app, String requestedRegionCode) {
        String regionCode = requestedRegionCode == null || requestedRegionCode.isBlank()
                ? app.getRegionCode()
                : requestedRegionCode.trim().toUpperCase();
        MarketRegion region = marketRegionRepository.findByCode(regionCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_REGION_NOT_SUPPORTED));
        if (!region.isEnabled() || !region.supports(app.getStoreType())) {
            throw new BusinessException(ErrorCode.STORE_REGION_NOT_SUPPORTED);
        }
        return regionCode;
    }

    private void applyExecution(AsoOrder order, Long adminId, LocalDateTime now) {
        if (!OrderStatus.PENDING_EXECUTION.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        order.setExecutedByAdminId(adminId);
        order.setExecutedAt(now);
        if (OrderType.KEYWORD_INSTALL.equals(order.getOrderType())) {
            order.setExpectedCompletedAt(now.plusHours(keywordExecutionHours(order)));
        }
        if (order.getExpectedCompletedAt() != null && !order.getExpectedCompletedAt().isAfter(now)) {
            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedAt(now);
        } else {
            order.setStatus(OrderStatus.EXECUTING);
        }
    }

    private int keywordExecutionHours(AsoOrder order) {
        Integer executionHours = order.getExecutionHours();
        return executionHours == null || executionHours < 1 ? 1 : executionHours;
    }

    @Transactional
    public int completeExpiredExecutingOrders() {
        LocalDateTime now = now();
        List<AsoOrder> dueOrders = orderRepository.findExecutingDueBefore(now);
        dueOrders.forEach(order -> {
            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedAt(now);
            orderRepository.update(order);
        });
        return dueOrders.size();
    }

    private PriceSnapshot calculateKeywordInstall(CreateOrderCommand command, CustomerApp app) {
        Map<KeywordRegionKey, Integer> keywordQuantities = keywordQuantities(command, app);
        if (keywordQuantities.isEmpty()) {
            throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
        }
        PricingConfig price = enabledPrice(PriceCode.KEYWORD_INSTALL);
        List<OrderItem> items = keywordQuantities.entrySet().stream()
                .map(entry -> item(
                        PriceCode.KEYWORD_INSTALL.name(),
                        entry.getKey().keyword(),
                        entry.getKey().regionCode(),
                        entry.getValue(),
                        price.getUnitPrice(),
                        price.getUnitPrice().multiply(BigDecimal.valueOf(entry.getValue())).setScale(2, RoundingMode.HALF_UP),
                        null
                ))
                .toList();
        String orderRegionCode = keywordQuantities.keySet().stream()
                .map(KeywordRegionKey::regionCode)
                .distinct()
                .limit(2)
                .count() > 1
                ? "MULTI"
                : keywordQuantities.keySet().iterator().next().regionCode();
        return new PriceSnapshot(
                PriceCode.KEYWORD_INSTALL,
                price.getUnitPrice(),
                keywordQuantities.values().stream().mapToInt(Integer::intValue).sum(),
                sumAmount(items),
                items,
                orderRegionCode,
                List.of()
        );
    }

    private Map<KeywordRegionKey, Integer> keywordQuantities(CreateOrderCommand command, CustomerApp app) {
        Map<KeywordRegionKey, Integer> result = new LinkedHashMap<>();
        if (command.keywordItems() != null && !command.keywordItems().isEmpty()) {
            command.keywordItems().stream()
                    .filter(item -> item != null && item.keyword() != null && !item.keyword().isBlank())
                    .forEach(item -> {
                        int quantity = item.quantity() == null ? 0 : item.quantity();
                        if (quantity > 0) {
                            String requestedRegionCode = item.regionCode() == null || item.regionCode().isBlank()
                                    ? command.regionCode()
                                    : item.regionCode();
                            String regionCode = resolveLinkedRegion(app, requestedRegionCode);
                            result.merge(new KeywordRegionKey(regionCode, item.keyword().trim()), quantity, Integer::sum);
                        }
                    });
            return result;
        }
        if (command.keywords() == null) {
            return result;
        }
        String regionCode = resolveLinkedRegion(app, command.regionCode());
        command.keywords().stream()
                .filter(keyword -> keyword != null && !keyword.isBlank())
                .map(String::trim)
                .distinct()
                .forEach(keyword -> result.put(new KeywordRegionKey(regionCode, keyword), 1));
        return result;
    }

    private PriceSnapshot calculateDownload(CreateOrderCommand command, CustomerApp app) {
        PricingConfig price = enabledPrice(PriceCode.DOWNLOAD);
        if (hasRegionItems(command)) {
            List<OrderItem> items = command.regionItems().stream()
                    .filter(item -> item != null)
                    .map(item -> {
                        int dailyDownloadCount = nonNegative(item.dailyDownloadCount());
                        if (dailyDownloadCount <= 0) {
                            return null;
                        }
                        int quantity = totalDays(command.startDate(), command.endDate()) * dailyDownloadCount;
                        return item(
                                PriceCode.DOWNLOAD.name(),
                                null,
                                resolveLinkedRegion(app, item.regionCode()),
                                quantity,
                                price.getUnitPrice(),
                                price.getUnitPrice().multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP),
                                "{\"dailyDownloadCount\":" + dailyDownloadCount + "}"
                        );
                    })
                    .filter(java.util.Objects::nonNull)
                    .toList();
            if (items.isEmpty()) {
                throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
            }
            return new PriceSnapshot(
                    PriceCode.DOWNLOAD,
                    price.getUnitPrice(),
                    items.stream().mapToInt(OrderItem::getQuantity).sum(),
                    sumAmount(items),
                    items,
                    orderRegionCode(items),
                    List.of()
            );
        }
        if (command.dailyDownloadCount() == null || command.dailyDownloadCount() <= 0) {
            throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
        }
        int quantity = totalDays(command.startDate(), command.endDate()) * command.dailyDownloadCount();
        OrderItem item = item(
                PriceCode.DOWNLOAD.name(),
                null,
                resolveLinkedRegion(app, command.regionCode()),
                quantity,
                price.getUnitPrice(),
                price.getUnitPrice().multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP),
                "{\"dailyDownloadCount\":" + command.dailyDownloadCount() + "}"
        );
        return new PriceSnapshot(
                PriceCode.DOWNLOAD,
                price.getUnitPrice(),
                quantity,
                item.getAmount(),
                List.of(item),
                item.getRegionCode(),
                List.of()
        );
    }

    private PriceSnapshot calculateRating(CreateOrderCommand command, CustomerApp app) {
        PricingConfig rating5Price = enabledPrice(PriceCode.RATING_5);
        PricingConfig rating4Price = enabledPrice(PriceCode.RATING_4);
        int days = totalDays(command.startDate(), command.endDate());
        if (hasRegionItems(command)) {
            List<OrderItem> items = command.regionItems().stream()
                    .filter(item -> item != null)
                    .flatMap(item -> ratingItems(
                            days * nonNegative(item.rating5Count()),
                            days * nonNegative(item.rating4Count()),
                            rating5Price,
                            rating4Price,
                            PriceCode.RATING_5,
                            PriceCode.RATING_4,
                            resolveLinkedRegion(app, item.regionCode())
                    ).stream())
                    .toList();
            if (items.isEmpty()) {
                throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
            }
            return new PriceSnapshot(
                    PriceCode.RATING_5,
                    null,
                    items.stream().mapToInt(OrderItem::getQuantity).sum(),
                    sumAmount(items),
                    items,
                    orderRegionCode(items),
                    List.of()
            );
        }
        int rating5 = nonNegative(command.rating5Count());
        int rating4 = nonNegative(command.rating4Count());
        if (rating5 + rating4 <= 0) {
            throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
        }
        String regionCode = resolveLinkedRegion(app, command.regionCode());
        List<OrderItem> items = ratingItems(days * rating5, days * rating4, rating5Price, rating4Price, PriceCode.RATING_5, PriceCode.RATING_4, regionCode);
        BigDecimal amount = sumAmount(items);
        return new PriceSnapshot(PriceCode.RATING_5, null, days * (rating5 + rating4), amount, items, regionCode, List.of());
    }

    private PriceSnapshot calculateReview(CreateOrderCommand command, CustomerApp app) {
        PricingConfig review5Price = enabledPrice(PriceCode.REVIEW_5);
        PricingConfig review4Price = enabledPrice(PriceCode.REVIEW_4);
        if (command.reviewDetails() != null && !command.reviewDetails().isEmpty()) {
            List<OrderCommentDetail> details = commentDetails(command, app);
            int review5 = (int) details.stream().filter(detail -> detail.getStarLevel() == 5).count();
            int review4 = (int) details.stream().filter(detail -> detail.getStarLevel() == 4).count();
            if (review5 + review4 <= 0) {
                throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
            }
            List<OrderItem> items = details.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            detail -> detail.getRegionCode() + ":" + detail.getStarLevel(),
                            LinkedHashMap::new,
                            java.util.stream.Collectors.toList()
                    ))
                    .entrySet()
                    .stream()
                    .map(entry -> {
                        OrderCommentDetail first = entry.getValue().get(0);
                        int count = entry.getValue().size();
                        PriceCode code = first.getStarLevel() == 5 ? PriceCode.REVIEW_5 : PriceCode.REVIEW_4;
                        PricingConfig price = first.getStarLevel() == 5 ? review5Price : review4Price;
                        return item(
                                code.name(),
                                null,
                                first.getRegionCode(),
                                count,
                                price.getUnitPrice(),
                                price.getUnitPrice().multiply(BigDecimal.valueOf(count)).setScale(2, RoundingMode.HALF_UP),
                                null
                        );
                    })
                    .toList();
            return new PriceSnapshot(
                    PriceCode.REVIEW_5,
                    null,
                    review5 + review4,
                    sumAmount(items),
                    items,
                    orderRegionCode(items),
                    details
            );
        }
        if (hasRegionItems(command)) {
            List<OrderItem> items = command.regionItems().stream()
                    .filter(item -> item != null)
                    .flatMap(item -> ratingItems(
                            nonNegative(item.review5Count()),
                            nonNegative(item.review4Count()),
                            review5Price,
                            review4Price,
                            PriceCode.REVIEW_5,
                            PriceCode.REVIEW_4,
                            resolveLinkedRegion(app, item.regionCode())
                    ).stream())
                    .toList();
            if (items.isEmpty()) {
                throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
            }
            return new PriceSnapshot(
                    PriceCode.REVIEW_5,
                    null,
                    items.stream().mapToInt(OrderItem::getQuantity).sum(),
                    sumAmount(items),
                    items,
                    orderRegionCode(items),
                    List.of()
            );
        }
        int review5 = nonNegative(command.review5Count());
        int review4 = nonNegative(command.review4Count());
        if (review5 + review4 <= 0) {
            throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
        }
        String regionCode = resolveLinkedRegion(app, command.regionCode());
        List<OrderItem> items = ratingItems(review5, review4, review5Price, review4Price, PriceCode.REVIEW_5, PriceCode.REVIEW_4, regionCode);
        BigDecimal amount = sumAmount(items);
        return new PriceSnapshot(PriceCode.REVIEW_5, null, review5 + review4, amount, items, regionCode, List.of());
    }

    private List<OrderCommentDetail> commentDetails(CreateOrderCommand command, CustomerApp app) {
        return command.reviewDetails().stream()
                .filter(detail -> detail != null)
                .map(detail -> {
                    String title = normalizeRequiredText(detail.commentTitle(), 120);
                    String content = normalizeRequiredText(detail.commentContent(), 1000);
                    int starLevel = detail.starLevel() == null ? 0 : detail.starLevel();
                    if (starLevel != 4 && starLevel != 5) {
                        throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
                    }
                    OrderCommentDetail result = new OrderCommentDetail();
                    result.setRegionCode(resolveLinkedRegion(app, detail.regionCode()));
                    result.setStarLevel(starLevel);
                    result.setCommentTitle(title);
                    result.setCommentContent(content);
                    return result;
                })
                .toList();
    }

    private List<OrderItem> ratingItems(
            int count5,
            int count4,
            PricingConfig price5,
            PricingConfig price4,
            PriceCode code5,
            PriceCode code4,
            String regionCode
    ) {
        java.util.ArrayList<OrderItem> items = new java.util.ArrayList<>();
        if (count5 > 0) {
            items.add(item(
                    code5.name(),
                    null,
                    regionCode,
                    count5,
                    price5.getUnitPrice(),
                    price5.getUnitPrice().multiply(BigDecimal.valueOf(count5)).setScale(2, RoundingMode.HALF_UP),
                    null
            ));
        }
        if (count4 > 0) {
            items.add(item(
                    code4.name(),
                    null,
                    regionCode,
                    count4,
                    price4.getUnitPrice(),
                    price4.getUnitPrice().multiply(BigDecimal.valueOf(count4)).setScale(2, RoundingMode.HALF_UP),
                    null
            ));
        }
        return items;
    }

    private boolean hasRegionItems(CreateOrderCommand command) {
        return command.regionItems() != null && !command.regionItems().isEmpty();
    }

    private String orderRegionCode(List<OrderItem> items) {
        List<String> regionCodes = items.stream()
                .map(OrderItem::getRegionCode)
                .filter(regionCode -> regionCode != null && !regionCode.isBlank())
                .distinct()
                .limit(2)
                .toList();
        if (regionCodes.size() > 1) {
            return "MULTI";
        }
        return regionCodes.isEmpty() ? null : regionCodes.get(0);
    }

    private OrderItem item(String itemType, String itemName, int quantity, BigDecimal unitPrice, BigDecimal amount, String metadataJson) {
        return item(itemType, itemName, null, quantity, unitPrice, amount, metadataJson);
    }

    private OrderItem item(String itemType, String itemName, String regionCode, int quantity, BigDecimal unitPrice, BigDecimal amount, String metadataJson) {
        OrderItem item = new OrderItem();
        item.setItemType(itemType);
        item.setItemName(itemName);
        item.setRegionCode(regionCode);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setAmount(amount);
        item.setMetadataJson(metadataJson);
        return item;
    }

    private BigDecimal sumAmount(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private List<AsoOrder> attachItems(List<AsoOrder> orders) {
        if (orders.isEmpty()) {
            return orders;
        }
        List<Long> orderIds = orders.stream().map(AsoOrder::getId).toList();
        Map<Long, List<OrderItem>> itemMap = orderItemRepository.findByOrderIds(orderIds);
        orders.forEach(order -> order.setItems(itemMap.getOrDefault(order.getId(), List.of())));
        return orders;
    }

    private AsoOrder attachItems(AsoOrder order) {
        Map<Long, List<OrderItem>> itemMap = orderItemRepository.findByOrderIds(List.of(order.getId()));
        order.setItems(itemMap.getOrDefault(order.getId(), List.of()));
        return order;
    }

    private List<AsoOrder> attachDetails(List<AsoOrder> orders) {
        if (orders.isEmpty()) {
            return orders;
        }
        List<Long> orderIds = orders.stream().map(AsoOrder::getId).toList();
        Map<Long, List<OrderCommentDetail>> detailMap = orderCommentDetailRepository.findByOrderIds(orderIds);
        orders.forEach(order -> order.setCommentDetails(detailMap.getOrDefault(order.getId(), List.of())));
        return orders;
    }

    private AsoOrder attachDetails(AsoOrder order) {
        Map<Long, List<OrderCommentDetail>> detailMap = orderCommentDetailRepository.findByOrderIds(List.of(order.getId()));
        order.setCommentDetails(detailMap.getOrDefault(order.getId(), List.of()));
        return order;
    }

    private List<AsoOrder> attachCustomers(List<AsoOrder> orders) {
        if (orders.isEmpty()) {
            return orders;
        }
        List<Long> customerIds = orders.stream()
                .map(AsoOrder::getCustomerId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (customerIds.isEmpty()) {
            return orders;
        }
        Map<Long, CustomerAccount> customers = customerAccountRepository.findByIds(customerIds).stream()
                .collect(Collectors.toMap(CustomerAccount::getId, Function.identity(), (left, right) -> left));
        orders.forEach(order -> applyCustomer(order, customers.get(order.getCustomerId())));
        return orders;
    }

    private AsoOrder attachCustomer(AsoOrder order) {
        if (order.getCustomerId() == null) {
            return order;
        }
        customerAccountRepository.findById(order.getCustomerId())
                .ifPresent(customer -> applyCustomer(order, customer));
        return order;
    }

    private void notifyOrderCreated(AsoOrder order) {
        attachCustomer(order);
        Runnable notification = () -> {
            try {
                orderNotificationSender.notifyOrderCreated(order);
            } catch (RuntimeException exception) {
                log.warn("Order notification failed after order committed. orderNo={}, errorType={}",
                        order.getOrderNo(),
                        exception.getClass().getSimpleName(),
                        exception);
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    CompletableFuture.runAsync(notification);
                }
            });
            return;
        }
        notification.run();
    }

    private void applyCustomer(AsoOrder order, CustomerAccount customer) {
        if (customer == null) {
            return;
        }
        order.setCustomerUsername(customer.getUsername());
        order.setCustomerEmail(customer.getEmail());
    }

    private PricingConfig enabledPrice(PriceCode code) {
        PricingConfig price = pricingConfigRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRICE_NOT_CONFIGURED));
        if (!price.isEnabled()) {
            throw new BusinessException(ErrorCode.PRICE_DISABLED);
        }
        return price;
    }

    private int totalDays(LocalDate startDate, LocalDate endDate) {
        return Math.toIntExact(ChronoUnit.DAYS.between(startDate, endDate.plusDays(1)));
    }

    private int nonNegative(Integer value) {
        if (value == null) {
            return 0;
        }
        if (value < 0) {
            throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
        }
        return value;
    }

    private String normalizeOptional(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        return normalized;
    }

    private String normalizeRequiredText(String value, int maxLength) {
        String normalized = normalizeOptional(value, maxLength);
        if (normalized == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        return normalized;
    }

    private String normalizeCancelReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "ADMIN_CANCELLED";
        }
        return reason.trim();
    }

    private String generateOrderNo() {
        return BusinessNumberGenerator.generate("YO", now());
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(clock.instant(), BUSINESS_ZONE);
    }

    private Page normalizePage(Integer page, Integer pageSize) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        return new Page(normalizedPage, normalizedPageSize);
    }

    private record Page(int page, int pageSize) {
        int offset() {
            return (page - 1) * pageSize;
        }
    }

    private record PriceSnapshot(
            PriceCode primaryPriceCode,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal totalAmount,
            List<OrderItem> items,
            String orderRegionCode,
            List<OrderCommentDetail> commentDetails
    ) {
    }

    private record KeywordRegionKey(
            String regionCode,
            String keyword
    ) {
    }
}
