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
import com.youou.aso.modules.order.domain.OrderEvent;
import com.youou.aso.modules.order.domain.OrderStatus;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.order.dto.CreateOrderCommand;
import com.youou.aso.modules.order.dto.OrderQuery;
import com.youou.aso.modules.order.repository.OrderCommentDetailRepository;
import com.youou.aso.modules.order.repository.OrderItemRepository;
import com.youou.aso.modules.order.repository.OrderEventRepository;
import com.youou.aso.modules.order.repository.OrderRepository;
import com.youou.aso.modules.pricing.domain.PriceCode;
import com.youou.aso.modules.pricing.domain.PricingConfig;
import com.youou.aso.modules.pricing.domain.OrderModuleConfig;
import com.youou.aso.modules.pricing.repository.PricingConfigRepository;
import com.youou.aso.modules.pricing.service.OrderModuleConfigService;
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
    private final OrderModuleConfigService orderModuleConfigService;
    private final CustomerAccountRepository customerAccountRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderEventRepository orderEventRepository;
    private final OrderCommentDetailRepository orderCommentDetailRepository;
    private final WalletService walletService;
    private final OrderNotificationSender orderNotificationSender;
    private final Clock clock;
    private final ReviewAttachmentService reviewAttachmentService;

    public OrderService(
            CustomerAppRepository customerAppRepository,
            MarketRegionRepository marketRegionRepository,
            PricingConfigRepository pricingConfigRepository,
            OrderModuleConfigService orderModuleConfigService,
            CustomerAccountRepository customerAccountRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderEventRepository orderEventRepository,
            OrderCommentDetailRepository orderCommentDetailRepository,
            WalletService walletService,
            OrderNotificationSender orderNotificationSender,
            Clock clock,
            ReviewAttachmentService reviewAttachmentService
    ) {
        this.customerAppRepository = customerAppRepository;
        this.marketRegionRepository = marketRegionRepository;
        this.pricingConfigRepository = pricingConfigRepository;
        this.orderModuleConfigService = orderModuleConfigService;
        this.customerAccountRepository = customerAccountRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderEventRepository = orderEventRepository;
        this.orderCommentDetailRepository = orderCommentDetailRepository;
        this.walletService = walletService;
        this.orderNotificationSender = orderNotificationSender;
        this.clock = clock;
        this.reviewAttachmentService = reviewAttachmentService;
    }

    @Transactional
    public AsoOrder createCustomerOrder(Long customerId, CreateOrderCommand command) {
        return createOrder(customerId, command, OrderStatus.PENDING_CONFIRM, null);
    }

    @Transactional
    public AsoOrder createAdminOrderForCustomer(Long customerId, Long adminId, CreateOrderCommand command) {
        if (adminId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        return createOrder(customerId, command, OrderStatus.PENDING_EXECUTION, adminId);
    }

    private AsoOrder createOrder(Long customerId, CreateOrderCommand command, OrderStatus paidStatus, Long confirmedByAdminId) {
        if (confirmedByAdminId == null) command = resolveImmediateStart(command);
        validateCreateCommand(customerId, command);
        if (confirmedByAdminId == null && !Boolean.TRUE.equals(command.startImmediately())) validateCustomerOrderTime(command.orderType(), scheduledStart(command));
        CustomerApp app = loadActiveCustomerApp(customerId, command.customerAppId());
        PriceSnapshot priceSnapshot = calculatePrice(command, app);
        AsoOrder order = buildOrder(customerId, app, command, priceSnapshot);
        order.setOrderNo(generateOrderNo());
        applyPaymentStatus(order, priceSnapshot, "ORDER_DEDUCT:" + command.orderType().name(), paidStatus, confirmedByAdminId);
        AsoOrder saved = orderRepository.save(order);
        walletService.linkTransactionToOrder(saved.getDeductedTransactionId(), saved.getId());
        orderItemRepository.saveAll(saved.getId(), priceSnapshot.items());
        orderCommentDetailRepository.saveAll(saved.getId(), priceSnapshot.commentDetails());
        saved.setItems(priceSnapshot.items());
        saved.setCommentDetails(priceSnapshot.commentDetails());
        reviewAttachmentService.bind(saved, command);
        recordLifecycleEvent(saved, "CREATED", confirmedByAdminId);
        notifyOrderCreated(saved);
        return saved;
    }

    @Transactional
    public AsoOrder resubmitEditableOrder(Long customerId, Long orderId, CreateOrderCommand command) {
        return editOrderDraft(customerId, orderId, command, null);
    }

    @Transactional
    public AsoOrder editAdminOrder(Long customerId, Long orderId, Long adminId, CreateOrderCommand command) {
        if (adminId == null) throw new BusinessException(ErrorCode.BAD_REQUEST);
        return editOrderDraft(customerId, orderId, command, adminId);
    }

    private AsoOrder editOrderDraft(Long customerId, Long orderId, CreateOrderCommand command, Long adminId) {
        if (adminId == null) command = resolveImmediateStart(command);
        validateCreateCommand(customerId, command);
        if (adminId == null && !Boolean.TRUE.equals(command.startImmediately())) validateCustomerOrderTime(command.orderType(), scheduledStart(command));
        if (orderId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        AsoOrder order = orderRepository.findByIdForUpdate(orderId)
                .filter(candidate -> customerId.equals(candidate.getCustomerId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        OrderStatus originalStatus = order.getStatus();
        boolean editable = adminId == null
                ? OrderStatus.PENDING_PAYMENT.equals(originalStatus) || OrderStatus.PENDING_CONFIRM.equals(originalStatus) || OrderStatus.CANCELLED.equals(originalStatus)
                : OrderStatus.PENDING_CONFIRM.equals(originalStatus) || OrderStatus.PENDING_EXECUTION.equals(originalStatus);
        if (!editable || order.getSourceAuditId() != null) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        if (adminId != null) {
            if (!order.getCustomerAppId().equals(command.customerAppId()) || order.getOrderType() != command.orderType()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST);
            }
            attachItems(order);
            if (hasRecordedProgress(order)) throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        CustomerApp app = loadActiveCustomerApp(customerId, command.customerAppId());
        PriceSnapshot priceSnapshot = calculatePrice(command, app);
        BigDecimal oldTotal = order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();
        int oldQuantity = order.getQuantity() == null ? 0 : order.getQuantity();
        boolean requiresFullPayment = OrderStatus.PENDING_PAYMENT.equals(originalStatus) || OrderStatus.CANCELLED.equals(originalStatus);
        if (OrderStatus.CANCELLED.equals(originalStatus)) {
            order.setRejectReason(null);
            order.setRefundTransactionId(null);
            order.setRefundAmount(BigDecimal.ZERO);
            order.setConfirmedByAdminId(null);
            order.setConfirmedAt(null);
            order.setExecutedByAdminId(null);
            order.setExecutedAt(null);
            order.setCompletedAt(null);
            order.setDeductedTransactionId(null);
        }
        if (requiresFullPayment) {
            applyPaymentStatus(order, priceSnapshot, "ORDER_DEDUCT_RETRY:" + order.getOrderNo(), OrderStatus.PENDING_CONFIRM, null);
        } else {
            BigDecimal difference = priceSnapshot.totalAmount().subtract(oldTotal);
            if (difference.signum() > 0) {
                WalletDebitResult debit = walletService.debitForOrder(customerId, difference, "ORDER_EDIT_INCREASE:" + order.getOrderNo());
                order.setBalanceBefore(debit.balanceBefore());
                order.setBalanceAfter(debit.balanceAfter());
                walletService.linkTransactionToOrder(debit.transactionId(), order.getId());
            } else if (difference.signum() < 0) {
                WalletDebitResult refund = walletService.refundForOrder(customerId, difference.abs(), "ORDER_EDIT_DECREASE:" + order.getOrderNo());
                order.setBalanceBefore(refund.balanceBefore());
                order.setBalanceAfter(refund.balanceAfter());
                walletService.linkTransactionToOrder(refund.transactionId(), order.getId());
            }
            order.setStatus(adminId == null ? OrderStatus.PENDING_CONFIRM : originalStatus);
        }
        copyOrderDraft(order, app, command, priceSnapshot);
        AsoOrder updated = orderRepository.updatePaymentDraft(order);
        if (requiresFullPayment) {
            walletService.linkTransactionToOrder(updated.getDeductedTransactionId(), updated.getId());
        }
        reviewAttachmentService.bind(updated, command);
        orderItemRepository.deleteByOrderId(updated.getId());
        orderCommentDetailRepository.deleteByOrderId(updated.getId());
        orderItemRepository.saveAll(updated.getId(), priceSnapshot.items());
        orderCommentDetailRepository.saveAll(updated.getId(), priceSnapshot.commentDetails());
        updated.setItems(priceSnapshot.items());
        updated.setCommentDetails(priceSnapshot.commentDetails());
        if (requiresFullPayment) {
            recordLifecycleEvent(updated, "RESUBMITTED", adminId);
            notifyOrderCreated(updated);
        } else {
            recordEvent(updated, "UPDATED", adminId, oldQuantity, updated.getQuantity(), 0, 0, oldTotal, updated.getTotalAmount());
        }
        return updated;
    }

    /** Kept for binary compatibility with callers compiled against the previous name. */
    @Transactional
    public AsoOrder resubmitPendingPaymentOrder(Long customerId, Long orderId, CreateOrderCommand command) {
        return resubmitEditableOrder(customerId, orderId, command);
    }

    @Transactional
    public AsoOrder payPendingPaymentOrder(Long customerId, Long orderId) {
        if (customerId == null || orderId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        AsoOrder order = orderRepository.findByIdForUpdate(orderId)
                .filter(candidate -> customerId.equals(candidate.getCustomerId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!OrderStatus.PENDING_PAYMENT.equals(order.getStatus()) || order.getSourceAuditId() != null) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        if (order.getConfirmedByAdminId() == null) {
            validateCustomerOrderTime(order.getOrderType(), order.getScheduledStartAt() != null
                    ? order.getScheduledStartAt() : order.getOrderStartDate().atStartOfDay());
        }
        OrderStatus paidStatus = order.getConfirmedByAdminId() == null
                ? OrderStatus.PENDING_CONFIRM
                : OrderStatus.PENDING_EXECUTION;
        PriceSnapshot currentPrice = new PriceSnapshot(
                order.getPricingCode(),
                order.getUnitPrice(),
                order.getQuantity() == null ? 0 : order.getQuantity(),
                order.getTotalAmount(),
                order.getItems() == null ? List.of() : order.getItems(),
                order.getRegionCode(),
                order.getCommentDetails() == null ? List.of() : order.getCommentDetails()
        );
        applyPaymentStatus(order, currentPrice, "ORDER_DEDUCT_RETRY:" + order.getOrderNo(), paidStatus, order.getConfirmedByAdminId());
        AsoOrder updated = orderRepository.update(order);
        walletService.linkTransactionToOrder(updated.getDeductedTransactionId(), updated.getId());
        if (!OrderStatus.PENDING_PAYMENT.equals(updated.getStatus())) {
            recordLifecycleEvent(updated, "PAID", null);
            notifyOrderCreated(updated);
        }
        return attachDetails(attachItems(updated));
    }

    @Transactional
    public AsoOrder confirmOrder(Long orderId, Long adminId) {
        AsoOrder order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!OrderStatus.PENDING_CONFIRM.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        order.setStatus(OrderStatus.PENDING_EXECUTION);
        order.setConfirmedByAdminId(adminId);
        order.setConfirmedAt(now());
        recordLifecycleEvent(order, "CONFIRMED", adminId);
        return orderRepository.update(order);
    }

    @Transactional
    public List<AsoOrder> confirmOrders(List<Long> orderIds, Long adminId) {
        if (orderIds == null || orderIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        List<AsoOrder> orders = orderIds.stream()
                .distinct()
                .sorted()
                .map(orderId -> orderRepository.findByIdForUpdate(orderId)
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
                    recordLifecycleEvent(order, "CONFIRMED", adminId);
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
        return attachEvents(attachDetails(attachItems(order)));
    }

    @Transactional
    public AsoOrder getAdminOrder(Long orderId) {
        if (orderId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        completeExpiredExecutingOrders();
        AsoOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return attachCustomer(attachEvents(attachDetails(attachItems(order))));
    }

    @Transactional
    public AsoOrder executeOrder(Long orderId, Long adminId) {
        AsoOrder order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        applyExecution(order, adminId, now());
        recordLifecycleEvent(order, "EXECUTED", adminId);
        return orderRepository.update(order);
    }

    @Transactional
    public List<AsoOrder> executeOrders(List<Long> orderIds, Long adminId) {
        if (orderIds == null || orderIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        List<AsoOrder> orders = orderIds.stream()
                .distinct()
                .sorted()
                .map(orderId -> orderRepository.findByIdForUpdate(orderId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND)))
                .toList();
        if (orders.stream().anyMatch(order -> !OrderStatus.PENDING_EXECUTION.equals(order.getStatus()))) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        LocalDateTime now = now();
        orders.forEach(order -> validateExecutionStart(order, now));
        return orders.stream()
                .map(order -> {
                    applyExecution(order, adminId, now);
                    recordLifecycleEvent(order, "EXECUTED", adminId);
                    return orderRepository.update(order);
                })
                .toList();
    }

    @Transactional
    public AsoOrder pauseOrder(Long orderId, Long adminId) {
        AsoOrder order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!OrderStatus.EXECUTING.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        order.setStatus(OrderStatus.PAUSED);
        AsoOrder updated = orderRepository.update(order);
        recordEvent(updated, "PAUSED", adminId, null, null, null, null, null, null);
        return attachItems(updated);
    }

    @Transactional
    public List<AsoOrder> pauseOrders(List<Long> orderIds, Long adminId) {
        if (orderIds == null || orderIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        List<AsoOrder> orders = orderIds.stream()
                .distinct()
                .sorted()
                .map(orderId -> orderRepository.findByIdForUpdate(orderId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND)))
                .toList();
        if (orders.stream().anyMatch(order -> !OrderStatus.EXECUTING.equals(order.getStatus()))) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        return orders.stream()
                .map(order -> {
                    order.setStatus(OrderStatus.PAUSED);
                    AsoOrder updated = orderRepository.update(order);
                    recordEvent(updated, "PAUSED", adminId, null, null, null, null, null, null);
                    return updated;
                })
                .toList();
    }

    @Transactional
    public AsoOrder resumeOrder(Long orderId, Long adminId) {
        AsoOrder order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!OrderStatus.PAUSED.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        order.setStatus(OrderStatus.EXECUTING);
        AsoOrder updated = orderRepository.update(order);
        recordEvent(updated, "RESUMED", adminId, null, null, null, null, null, null);
        return updated;
    }

    @Transactional
    public AsoOrder updatePausedOrder(Long orderId, Long adminId, List<PausedItemEdit> edits) {
        return updatePausedOrder(orderId, adminId, edits, false);
    }

    @Transactional
    public AsoOrder closePausedOrder(Long orderId, Long adminId, List<PausedItemEdit> edits) {
        return updatePausedOrder(orderId, adminId, edits, true);
    }

    private AsoOrder updatePausedOrder(Long orderId, Long adminId, List<PausedItemEdit> edits, boolean close) {
        if (adminId == null) throw new BusinessException(ErrorCode.BAD_REQUEST);
        AsoOrder order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!OrderStatus.PAUSED.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        attachItems(order);
        if (edits == null || edits.isEmpty() || edits.size() != order.getItems().size()
                || edits.stream().anyMatch(edit -> edit == null || edit.itemId() == null)) {
            throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
        }
        Map<Long, PausedItemEdit> editByItemId;
        try {
            editByItemId = edits.stream().collect(java.util.stream.Collectors.toMap(PausedItemEdit::itemId, edit -> edit));
        } catch (IllegalStateException exception) {
            throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
        }
        Integer oldQuantity = order.getQuantity();
        BigDecimal oldTotal = order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();
        Integer oldCompleted = order.getItems().stream()
                .map(item -> item.getCompletedQuantity() == null ? 0 : item.getCompletedQuantity())
                .reduce(0, Integer::sum);
        for (OrderItem item : order.getItems()) {
            PausedItemEdit edit = editByItemId.get(item.getId());
            if (edit == null || edit.quantity() == null || edit.quantity() < (close ? 0 : 1)
                    || (close && (item.getQuantity() == null || edit.quantity() > item.getQuantity()))
                    || edit.completedQuantity() == null || edit.completedQuantity() < 0
                    || edit.completedQuantity() > edit.quantity()) {
                throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
            }
        }
        for (OrderItem item : order.getItems()) {
            PausedItemEdit edit = editByItemId.get(item.getId());
            item.setQuantity(edit.quantity());
            item.setCompletedQuantity(close ? edit.quantity() : edit.completedQuantity());
            item.setAmount(item.getUnitPrice().multiply(BigDecimal.valueOf(edit.quantity()))
                    .setScale(2, RoundingMode.HALF_UP));
        }
        BigDecimal newTotal = sumAmount(order.getItems()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal difference = newTotal.subtract(oldTotal).setScale(2, RoundingMode.HALF_UP);
        if (difference.signum() > 0) {
            WalletDebitResult debit = walletService.debitForOrder(
                    order.getCustomerId(),
                    difference,
                    "ORDER_QUANTITY_INCREASE:" + order.getOrderNo()
            );
            order.setBalanceBefore(debit.balanceBefore());
            order.setBalanceAfter(debit.balanceAfter());
            walletService.linkTransactionToOrder(debit.transactionId(), order.getId());
        } else if (difference.signum() < 0) {
            WalletDebitResult refund = walletService.refundForOrder(
                    order.getCustomerId(),
                    difference.abs(),
                    "ORDER_QUANTITY_DECREASE:" + order.getOrderNo()
            );
            order.setBalanceBefore(refund.balanceBefore());
            order.setBalanceAfter(refund.balanceAfter());
            walletService.linkTransactionToOrder(refund.transactionId(), order.getId());
        }
        order.setQuantity(order.getItems().stream().map(OrderItem::getQuantity).reduce(0, Integer::sum));
        order.setTotalAmount(newTotal);
        if (close) {
            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedAt(now());
        }
        orderItemRepository.updateQuantitiesAndProgress(order.getItems());
        AsoOrder updated = orderRepository.update(order);
        Integer newCompleted = updated.getItems().stream()
                .map(item -> item.getCompletedQuantity() == null ? 0 : item.getCompletedQuantity())
                .reduce(0, Integer::sum);
        recordEvent(updated, close ? "CLOSED" : "UPDATED", adminId, oldQuantity, updated.getQuantity(),
                oldCompleted, newCompleted, oldTotal, newTotal);
        return attachItems(updated);
    }
    @Transactional
    public AsoOrder cancelOrder(Long orderId, Long adminId, String reason) {
        AsoOrder order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!OrderStatus.PENDING_CONFIRM.equals(order.getStatus())
                && !OrderStatus.PENDING_EXECUTION.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        if (OrderStatus.PENDING_EXECUTION.equals(order.getStatus())) {
            attachItems(order);
            if (hasRecordedProgress(order)) {
                throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
            }
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
            order.setBalanceBefore(refund.balanceBefore());
            order.setBalanceAfter(refund.balanceAfter());
            walletService.linkTransactionToOrder(refund.transactionId(), order.getId());
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setRejectReason(normalizeCancelReason(reason));
        if (order.getRefundTransactionId() != null) order.setRefundAmount(order.getTotalAmount());
        recordLifecycleEvent(order, "CANCELLED", adminId);
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
        OrderModuleConfig module = resolveOrderModule(command);
        order.setOrderModuleId(module == null ? null : module.id());
        order.setOrderModuleName(module == null ? null : module.moduleName());
        order.setPricingCode(priceSnapshot.primaryPriceCode());
        order.setStoreType(app.getStoreType());
        order.setRegionCode(regionCode);
        order.setAppIdentifier(app.getAppIdentifier());
        order.setAppName(app.getAppName());
        order.setAppIconUrl(app.getAppIconUrl());
        order.setScheduledStartAt(scheduledStart(command));
        order.setOrderStartDate(command.startDate());
        order.setOrderEndDate(command.endDate());
        order.setExecutionHours(command.executionHours());
        order.setTotalDays(totalDays(command.startDate(), command.endDate()));
        order.setQuantity(priceSnapshot.quantity());
        order.setUnitPrice(priceSnapshot.unitPrice());
        order.setTotalAmount(priceSnapshot.totalAmount());
        order.setExpectedCompletedAt(OrderType.KEYWORD_INSTALL.equals(command.orderType())
                ? order.getScheduledStartAt().plusHours(keywordExecutionHours(order))
                : command.endDate().plusDays(1).atStartOfDay());
    }

    private void applyPaymentStatus(AsoOrder order, PriceSnapshot priceSnapshot, String remark) {
        applyPaymentStatus(order, priceSnapshot, remark, OrderStatus.PENDING_CONFIRM, null);
    }

    private void applyPaymentStatus(
            AsoOrder order,
            PriceSnapshot priceSnapshot,
            String remark,
            OrderStatus paidStatus,
            Long confirmedByAdminId
    ) {
        if (priceSnapshot.totalAmount() == null || priceSnapshot.totalAmount().signum() <= 0) {
            throw new BusinessException(ErrorCode.PRICE_INVALID);
        }
        if (OrderStatus.PENDING_EXECUTION.equals(paidStatus)) {
            order.setConfirmedByAdminId(confirmedByAdminId);
        }
        try {
            WalletDebitResult debit = walletService.debitForOrder(
                    order.getCustomerId(),
                    priceSnapshot.totalAmount(),
                    remark
            );
            order.setStatus(paidStatus);
            if (OrderStatus.PENDING_EXECUTION.equals(paidStatus)) {
                order.setConfirmedAt(now());
            }
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
        OrderModuleConfig module = resolveOrderModule(command);
        if (module != null && !module.storeTypes().contains(app.getStoreType())) throw new BusinessException(ErrorCode.STORE_REGION_NOT_SUPPORTED);
        return switch (command.orderType()) {
            case KEYWORD_INSTALL -> calculateKeywordInstall(command, app, module);
            case DOWNLOAD -> calculateDownload(command, app, module);
            case RATING -> calculateRating(command, app, module);
            case REVIEW -> calculateReview(command, app, module);
            case RANK_GUARANTEE, CHART_RANK_GUARANTEE, KEYWORD_COVERAGE -> throw new BusinessException(ErrorCode.SPECIAL_AUDIT_NOT_APPROVED);
        };
    }

    private OrderModuleConfig resolveOrderModule(CreateOrderCommand command) {
        if (command.orderModuleId() == null) return null;
        if (orderModuleConfigService == null) throw new BusinessException(ErrorCode.PRICE_INVALID);
        return orderModuleConfigService.requireEnabled(command.orderModuleId(), command.orderType());
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
        validateExecutionStart(order, now);
        order.setExecutedByAdminId(adminId);
        order.setExecutedAt(now);
        if (OrderType.KEYWORD_INSTALL.equals(order.getOrderType())) {
            LocalDateTime scheduledStart = order.getScheduledStartAt() != null ? order.getScheduledStartAt()
                    : order.getOrderStartDate() == null ? now : order.getOrderStartDate().atStartOfDay();
            LocalDateTime executionStart = scheduledStart.isAfter(now) ? scheduledStart : now;
            order.setExpectedCompletedAt(executionStart.plusHours(keywordExecutionHours(order)));
        }
        if (order.getExpectedCompletedAt() != null && !order.getExpectedCompletedAt().isAfter(now)) {
            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedAt(now);
        } else {
            order.setStatus(OrderStatus.EXECUTING);
        }
    }

    private void validateExecutionStart(AsoOrder order, LocalDateTime now) {
        if (order.getOrderType() != OrderType.KEYWORD_INSTALL && order.getOrderType() != OrderType.DOWNLOAD
                && order.getOrderType() != OrderType.RATING && order.getOrderType() != OrderType.REVIEW) return;
        LocalDateTime start = order.getOrderType() == OrderType.KEYWORD_INSTALL && order.getScheduledStartAt() != null
                ? order.getScheduledStartAt()
                : order.getOrderStartDate() == null ? null : order.getOrderStartDate().atStartOfDay();
        if (start != null && start.isAfter(now)) throw new BusinessException(ErrorCode.ORDER_NOT_STARTED);
    }

    private CreateOrderCommand resolveImmediateStart(CreateOrderCommand command) {
        if (command == null || command.orderType() != OrderType.KEYWORD_INSTALL
                || !Boolean.TRUE.equals(command.startImmediately())) return command;
        LocalDateTime start = now().truncatedTo(ChronoUnit.MINUTES);
        return new CreateOrderCommand(command.customerAppId(), command.regionCode(), command.orderType(), start.toLocalDate(), start.toLocalDate(), command.executionHours(), command.keywords(), command.keywordItems(), command.regionItems(), command.reviewDetails(), command.dailyDownloadCount(), command.rating5Count(), command.rating4Count(), command.review5Count(), command.review4Count(), command.orderModuleId(), start, true);
    }

    private LocalDateTime scheduledStart(CreateOrderCommand command) {
        if (!OrderType.KEYWORD_INSTALL.equals(command.orderType())) return null;
        LocalDateTime scheduled = command.scheduledStartAt() == null
                ? command.startDate().atStartOfDay() : command.scheduledStartAt().truncatedTo(ChronoUnit.MINUTES);
        if (!scheduled.toLocalDate().equals(command.startDate())) {
            throw new BusinessException(ErrorCode.ORDER_DATE_INVALID);
        }
        return scheduled;
    }

    private void validateCustomerOrderTime(OrderType type, LocalDateTime scheduled) {
        if (OrderType.KEYWORD_INSTALL.equals(type)
                && (scheduled == null || scheduled.isBefore(now().truncatedTo(ChronoUnit.MINUTES)))) {
            throw new BusinessException(ErrorCode.ORDER_TIME_IN_PAST);
        }
    }

    private int keywordExecutionHours(AsoOrder order) {
        Integer executionHours = order.getExecutionHours();
        return executionHours == null || executionHours < 1 ? 1 : executionHours;
    }

    @Transactional
    public int completeExpiredExecutingOrders() {
        LocalDateTime now = now();
        List<AsoOrder> dueOrders = orderRepository.findDueBefore(now);
        dueOrders.forEach(order -> {
            attachItems(order);
            if (OrderStatus.PAUSED.equals(order.getStatus())
                    || (OrderStatus.PENDING_EXECUTION.equals(order.getStatus()) && hasRecordedProgress(order))) {
                refundUnfinishedQuantity(order);
            } else {
                order.getItems().forEach(item -> item.setCompletedQuantity(item.getQuantity()));
                orderItemRepository.updateCompletedQuantities(order.getItems());
            }
            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedAt(now);
            recordLifecycleEvent(order, "COMPLETED", null);
            orderRepository.update(order);
        });
        return dueOrders.size();
    }

    private boolean hasRecordedProgress(AsoOrder order) {
        return order.getItems().stream().anyMatch(item -> item.getCompletedQuantity() != null);
    }

    private void refundUnfinishedQuantity(AsoOrder order) {
        if (order.getRefundTransactionId() != null) {
            return;
        }
        BigDecimal remainingAmount = order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(
                        item.getCompletedQuantity() == null ? 0 : item.getCompletedQuantity())).setScale(2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal refundAmount = order.getTotalAmount().subtract(remainingAmount).max(BigDecimal.ZERO);
        order.setRefundAmount(refundAmount);
        if (refundAmount.signum() == 0) {
            return;
        }
        WalletDebitResult refund = walletService.refundForOrder(order.getCustomerId(), refundAmount,
                "ORDER_REFUND:" + order.getOrderNo());
        order.setRefundTransactionId(refund.transactionId());
        walletService.linkTransactionToOrder(refund.transactionId(), order.getId());
    }

    public record PausedItemEdit(Long itemId, Integer quantity, Integer completedQuantity) {
    }

    public record CompletedItemEdit(Long itemId, Integer completedQuantity) {
    }

    @Transactional
    public AsoOrder adjustCompletedOrder(Long orderId, Long adminId, List<CompletedItemEdit> edits, String reason) {
        if (reason == null || reason.isBlank() || reason.length() > 500) throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        if (adminId == null) throw new BusinessException(ErrorCode.BAD_REQUEST);
        AsoOrder order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getStatus() != OrderStatus.COMPLETED) throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        attachItems(order);
        if (edits == null || edits.isEmpty() || edits.size() != order.getItems().size()
                || edits.stream().anyMatch(edit -> edit == null || edit.itemId() == null || edit.completedQuantity() == null)) {
            throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
        }
        Map<Long, CompletedItemEdit> byId;
        try {
            byId = edits.stream().collect(java.util.stream.Collectors.toMap(CompletedItemEdit::itemId, edit -> edit));
        } catch (IllegalStateException exception) {
            throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
        }
        if (order.getTotalAmount() == null) throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
        BigDecimal previousRefund = order.getRefundAmount() == null ? BigDecimal.ZERO : order.getRefundAmount();
        BigDecimal remainingAmount = BigDecimal.ZERO;
        int oldCompleted = 0;
        int newCompleted = 0;
        for (OrderItem item : order.getItems()) {
            if (item.getQuantity() == null || item.getQuantity() < 0) throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
            int previous = item.getCompletedQuantity() != null ? item.getCompletedQuantity()
                    : order.getRefundTransactionId() != null || previousRefund.signum() > 0 ? 0 : item.getQuantity();
            CompletedItemEdit edit = byId.get(item.getId());
            if (edit == null || edit.completedQuantity() < 0 || edit.completedQuantity() > previous
                    || item.getUnitPrice() == null || item.getUnitPrice().signum() < 0) {
                throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
            }
            oldCompleted += previous;
            newCompleted += edit.completedQuantity();
            remainingAmount = remainingAmount.add(item.getUnitPrice().multiply(BigDecimal.valueOf(edit.completedQuantity()))
                    .setScale(2, RoundingMode.HALF_UP));
        }
        if (newCompleted == oldCompleted) return order;
        BigDecimal cumulativeRefund = order.getTotalAmount().subtract(remainingAmount).max(previousRefund);
        BigDecimal refundAmount = cumulativeRefund.subtract(previousRefund);
        if (cumulativeRefund.compareTo(order.getTotalAmount()) > 0) {
            throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
        }
        if (refundAmount.signum() > 0) {
            WalletDebitResult refund = walletService.refundForOrder(order.getCustomerId(), refundAmount,
                    "ORDER_COMPLETION_ADJUSTMENT:" + order.getOrderNo());
            order.setBalanceBefore(refund.balanceBefore());
            order.setBalanceAfter(refund.balanceAfter());
            order.setRefundTransactionId(refund.transactionId());
            walletService.linkTransactionToOrder(refund.transactionId(), orderId);
        }
        order.setRefundAmount(cumulativeRefund);
        order.getItems().forEach(item -> item.setCompletedQuantity(byId.get(item.getId()).completedQuantity()));
        orderItemRepository.updateCompletedQuantities(order.getItems());
        AsoOrder updated = orderRepository.update(order);
        recordEvent(updated, "COMPLETION_ADJUSTED", adminId, order.getQuantity(), order.getQuantity(),
                oldCompleted, newCompleted, order.getTotalAmount().subtract(previousRefund), order.getTotalAmount().subtract(cumulativeRefund), reason.trim());
        return attachItems(updated);
    }

    private PriceSnapshot calculateKeywordInstall(CreateOrderCommand command, CustomerApp app, OrderModuleConfig module) {
        Map<KeywordRegionKey, Integer> keywordQuantities = keywordQuantities(command, app);
        if (keywordQuantities.isEmpty()) {
            throw new BusinessException(ErrorCode.ORDER_QUANTITY_INVALID);
        }
        validateRegionCombination(keywordQuantities.keySet().stream().map(KeywordRegionKey::regionCode).toList());
        PricingConfig price = enabledPrice(PriceCode.KEYWORD_INSTALL);
        List<OrderItem> items = keywordQuantities.entrySet().stream()
                .map(entry -> {
                    BigDecimal itemUnitPrice = unitPriceForRegion(price, entry.getKey().regionCode(), module);
                    return item(
                            PriceCode.KEYWORD_INSTALL.name(),
                            entry.getKey().keyword(),
                            entry.getKey().regionCode(),
                            entry.getValue(),
                            itemUnitPrice,
                            itemUnitPrice.multiply(BigDecimal.valueOf(entry.getValue())).setScale(2, RoundingMode.HALF_UP),
                            null
                    );
                })
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
                items.stream().map(OrderItem::getUnitPrice).distinct().limit(2).count() == 1 ? items.get(0).getUnitPrice() : null,
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

    private PriceSnapshot calculateDownload(CreateOrderCommand command, CustomerApp app, OrderModuleConfig module) {
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
                        String regionCode = resolveLinkedRegion(app, item.regionCode());
                        BigDecimal unitPrice = unitPriceForRegion(price, regionCode, module);
                        return item(
                                PriceCode.DOWNLOAD.name(),
                                null,
                                regionCode,
                                quantity,
                                unitPrice,
                                unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP),
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
                    module == null ? price.getUnitPrice() : module.unitPrice(),
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
        String downloadRegionCode = resolveLinkedRegion(app, command.regionCode());
        BigDecimal downloadUnitPrice = unitPriceForRegion(price, downloadRegionCode, module);
        OrderItem item = item(
                PriceCode.DOWNLOAD.name(),
                null,
                downloadRegionCode,
                quantity,
                downloadUnitPrice,
                downloadUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP),
                "{\"dailyDownloadCount\":" + command.dailyDownloadCount() + "}"
        );
        return new PriceSnapshot(
                PriceCode.DOWNLOAD,
                module == null ? price.getUnitPrice() : module.unitPrice(),
                quantity,
                item.getAmount(),
                List.of(item),
                item.getRegionCode(),
                List.of()
        );
    }

    private PriceSnapshot calculateRating(CreateOrderCommand command, CustomerApp app, OrderModuleConfig module) {
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
                            resolveOrderRegion(app, item.regionCode(), OrderType.RATING),
                            module
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
        String regionCode = resolveOrderRegion(app, command.regionCode(), OrderType.RATING);
        List<OrderItem> items = ratingItems(days * rating5, days * rating4, rating5Price, rating4Price, PriceCode.RATING_5, PriceCode.RATING_4, regionCode, module);
        BigDecimal amount = sumAmount(items);
        return new PriceSnapshot(PriceCode.RATING_5, null, days * (rating5 + rating4), amount, items, regionCode, List.of());
    }

    private PriceSnapshot calculateReview(CreateOrderCommand command, CustomerApp app, OrderModuleConfig module) {
        PricingConfig review5Price = enabledPrice(PriceCode.REVIEW_5);
        PricingConfig review4Price = enabledPrice(PriceCode.REVIEW_4);
        if (command.reviewDetails() != null && !command.reviewDetails().isEmpty()) {
            List<OrderCommentDetail> details = commentDetails(command, app);
            validateRegionCombination(details.stream().map(OrderCommentDetail::getRegionCode).toList());
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
                        BigDecimal unitPrice = unitPriceForRegion(price, first.getRegionCode(), module);
                        return item(
                                code.name(),
                                null,
                                first.getRegionCode(),
                                count,
                                unitPrice,
                                unitPrice.multiply(BigDecimal.valueOf(count)).setScale(2, RoundingMode.HALF_UP),
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
                            resolveOrderRegion(app, item.regionCode(), OrderType.REVIEW),
                            module
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
        String regionCode = resolveOrderRegion(app, command.regionCode(), OrderType.REVIEW);
        List<OrderItem> items = ratingItems(review5, review4, review5Price, review4Price, PriceCode.REVIEW_5, PriceCode.REVIEW_4, regionCode, module);
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
                    result.setRegionCode(resolveOrderRegion(app, detail.regionCode(), OrderType.REVIEW));
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
            String regionCode,
            OrderModuleConfig module
    ) {
        java.util.ArrayList<OrderItem> items = new java.util.ArrayList<>();
        if (count5 > 0) {
            BigDecimal effectivePrice5 = unitPriceForRegion(price5, regionCode, module);
            items.add(item(
                    code5.name(),
                    null,
                    regionCode,
                    count5,
                    effectivePrice5,
                    effectivePrice5.multiply(BigDecimal.valueOf(count5)).setScale(2, RoundingMode.HALF_UP),
                    null
            ));
        }
        if (count4 > 0) {
            BigDecimal effectivePrice4 = unitPriceForRegion(price4, regionCode, module);
            items.add(item(
                    code4.name(),
                    null,
                    regionCode,
                    count4,
                    effectivePrice4,
                    effectivePrice4.multiply(BigDecimal.valueOf(count4)).setScale(2, RoundingMode.HALF_UP),
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
        validateRegionCombination(regionCodes);
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
        Map<Long, Map<String, String>> moduleNames = new java.util.HashMap<>();
        if (orderModuleConfigService != null && orders.stream().anyMatch(order -> order.getOrderModuleId() != null)) {
            for (OrderModuleConfig module : orderModuleConfigService.listAll()) {
                Map<String, String> names = new java.util.HashMap<>();
                String[] locales = {"zh-CN", "en-US", "ru-RU", "pt-PT", "es-ES"};
                String[] values = {module.moduleName(), module.moduleNameEn(), module.moduleNameRu(), module.moduleNamePt(), module.moduleNameEs()};
                for (int i = 0; i < locales.length; i++) {
                    if (values[i] != null && !values[i].isBlank()) names.put(locales[i], values[i].trim());
                }
                moduleNames.put(module.id(), Map.copyOf(names));
            }
        }
        orders.forEach(order -> {
            order.setCommentDetails(List.of());
            order.setOrderModuleNames(moduleNames.getOrDefault(order.getOrderModuleId(), Map.of()));
        });
        return orders;
    }

    private AsoOrder attachDetails(AsoOrder order) {
        attachDetails(List.of(order));
        return order;
    }

    private AsoOrder attachEvents(AsoOrder order) {
        order.setEvents(orderEventRepository.findByOrderId(order.getId()));
        return order;
    }

    private void recordLifecycleEvent(AsoOrder order, String type, Long adminId) {
        recordEvent(order, type, adminId, null, order.getQuantity(), null, null, null, order.getTotalAmount());
    }

    private void recordEvent(
            AsoOrder order,
            String eventType,
            Long adminId,
            Integer quantityBefore,
            Integer quantityAfter,
            Integer completedBefore,
            Integer completedAfter,
            BigDecimal amountBefore,
            BigDecimal amountAfter
    ) {
        recordEvent(order, eventType, adminId, quantityBefore, quantityAfter, completedBefore, completedAfter, amountBefore, amountAfter, null);
    }
    private void recordEvent(
            AsoOrder order,
            String eventType,
            Long adminId,
            Integer quantityBefore,
            Integer quantityAfter,
            Integer completedBefore,
            Integer completedAfter,
            BigDecimal amountBefore,
            BigDecimal amountAfter,
            String reason
    ) {
        OrderEvent event = new OrderEvent();
        event.setOrderId(order.getId());
        event.setEventType(eventType);
        event.setReason(reason);
        event.setQuantityBefore(quantityBefore);
        event.setQuantityAfter(quantityAfter);
        event.setCompletedBefore(completedBefore);
        event.setCompletedAfter(completedAfter);
        event.setAmountBefore(amountBefore);
        event.setAmountAfter(amountAfter);
        event.setCreatedByAdminId(adminId);
        orderEventRepository.save(event);
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

    private BigDecimal unitPriceForRegion(PricingConfig price, String regionCode) {
        ensureRegionAllowed(orderTypeFor(price.getCode()), regionCode);
        return pricingConfigRepository.findRegionPrice(price.getCode(), regionCode)
                .orElseGet(() -> "CN".equalsIgnoreCase(regionCode) ? price.getChinaUnitPrice() : price.getUnitPrice());
    }

    private BigDecimal unitPriceForRegion(PricingConfig price, String regionCode, OrderModuleConfig module) {
        if (module == null) return unitPriceForRegion(price, regionCode);
        if (!pricingConfigRepository.findModuleRegions(module.id()).contains(regionCode)) throw new BusinessException(ErrorCode.STORE_REGION_NOT_SUPPORTED);
        BigDecimal priceValue = pricingConfigRepository.findModulePrices(module.id(),price.getCode()).getOrDefault(regionCode,module.priceFor(regionCode));
        if (priceValue == null || priceValue.signum() <= 0) throw new BusinessException(ErrorCode.PRICE_INVALID);
        return priceValue;
    }

    private void ensureRegionAllowed(OrderType orderType, String regionCode) {
        if (pricingConfigRepository.hasRegionConfiguration(orderType)
                && !pricingConfigRepository.findAllowedRegionCodes(orderType).contains(regionCode.toUpperCase())) {
            throw new BusinessException(ErrorCode.STORE_REGION_NOT_SUPPORTED);
        }
    }

    private OrderType orderTypeFor(PriceCode code) {
        return switch (code) {
            case KEYWORD_INSTALL -> OrderType.KEYWORD_INSTALL;
            case DOWNLOAD -> OrderType.DOWNLOAD;
            case RATING_5, RATING_4 -> OrderType.RATING;
            case REVIEW_5, REVIEW_4 -> OrderType.REVIEW;
        };
    }

    private String resolveOrderRegion(CustomerApp app, String requestedRegionCode, OrderType orderType) {
        String regionCode = resolveLinkedRegion(app, requestedRegionCode);
        if ((OrderType.RATING.equals(orderType) || OrderType.REVIEW.equals(orderType))
                && "CN".equalsIgnoreCase(regionCode)) {
            throw new BusinessException(ErrorCode.STORE_REGION_NOT_SUPPORTED);
        }
        return regionCode;
    }

    private void validateRegionCombination(java.util.Collection<String> regionCodes) {
        java.util.Set<String> distinct = regionCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(code -> code.trim().toUpperCase())
                .collect(java.util.stream.Collectors.toSet());
        if (distinct.contains("CN") && distinct.size() > 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
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
