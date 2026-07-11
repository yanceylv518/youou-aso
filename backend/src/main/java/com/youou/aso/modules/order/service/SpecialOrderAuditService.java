package com.youou.aso.modules.order.service;

import com.youou.aso.common.api.PageResult;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.common.util.BusinessNumberGenerator;
import com.youou.aso.modules.appmanagement.domain.CustomerApp;
import com.youou.aso.modules.appmanagement.domain.CustomerAppStatus;
import com.youou.aso.modules.appmanagement.domain.MarketRegion;
import com.youou.aso.modules.appmanagement.repository.CustomerAppRepository;
import com.youou.aso.modules.appmanagement.repository.MarketRegionRepository;
import com.youou.aso.modules.order.domain.AsoOrder;
import com.youou.aso.modules.order.domain.OrderItem;
import com.youou.aso.modules.order.domain.OrderStatus;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.order.domain.SpecialAuditStatus;
import com.youou.aso.modules.order.domain.SpecialOrderAudit;
import com.youou.aso.modules.order.domain.SpecialOrderAuditItem;
import com.youou.aso.modules.order.dto.ReviewSpecialAuditCommand;
import com.youou.aso.modules.order.dto.SubmitSpecialAuditCommand;
import com.youou.aso.modules.order.repository.OrderItemRepository;
import com.youou.aso.modules.order.repository.OrderRepository;
import com.youou.aso.modules.order.repository.SpecialOrderAuditItemRepository;
import com.youou.aso.modules.order.repository.SpecialOrderAuditRepository;
import com.youou.aso.modules.wallet.service.WalletDebitResult;
import com.youou.aso.modules.wallet.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class SpecialOrderAuditService {
    private static final Logger log = LoggerFactory.getLogger(SpecialOrderAuditService.class);
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");

    private final CustomerAppRepository customerAppRepository;
    private final MarketRegionRepository marketRegionRepository;
    private final SpecialOrderAuditRepository specialOrderAuditRepository;
    private final SpecialOrderAuditItemRepository specialOrderAuditItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WalletService walletService;
    private final OrderNotificationSender orderNotificationSender;
    private final Clock clock;

    public SpecialOrderAuditService(
            CustomerAppRepository customerAppRepository,
            MarketRegionRepository marketRegionRepository,
            SpecialOrderAuditRepository specialOrderAuditRepository,
            SpecialOrderAuditItemRepository specialOrderAuditItemRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            WalletService walletService,
            OrderNotificationSender orderNotificationSender,
            Clock clock
    ) {
        this.customerAppRepository = customerAppRepository;
        this.marketRegionRepository = marketRegionRepository;
        this.specialOrderAuditRepository = specialOrderAuditRepository;
        this.specialOrderAuditItemRepository = specialOrderAuditItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.walletService = walletService;
        this.orderNotificationSender = orderNotificationSender;
        this.clock = clock;
    }

    @Transactional
    public SpecialOrderAudit submitCustomerAudit(Long customerId, SubmitSpecialAuditCommand command) {
        if (customerId == null || command == null || command.customerAppId() == null || command.orderType() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        if (!isSpecialOrderType(command.orderType())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        CustomerApp app = customerAppRepository.findById(command.customerAppId())
                .filter(candidate -> customerId.equals(candidate.getCustomerId()))
                .filter(candidate -> CustomerAppStatus.ACTIVE.equals(candidate.getStatus()))
                .orElseThrow(() -> new BusinessException(ErrorCode.APP_NOT_VERIFIED));
        String regionCode = resolveLinkedRegion(app, command.regionCode());
        String requestedContent = normalizeOrFallback(command.requestedContent(), defaultAdminSpecialContent(command.orderType(), app));

        SpecialOrderAudit audit = new SpecialOrderAudit();
        audit.setAuditNo(generateAuditNo());
        audit.setCustomerId(customerId);
        audit.setCustomerAppId(app.getId());
        audit.setOrderType(command.orderType());
        audit.setStoreType(app.getStoreType());
        audit.setRegionCode(regionCode);
        audit.setAppIdentifier(app.getAppIdentifier());
        audit.setAppName(app.getAppName());
        audit.setAppIconUrl(app.getAppIconUrl());
        audit.setRequestedContent(requestedContent);
        audit.setContactType(normalizeOptional(command.contactType(), 32));
        audit.setContactValue(normalizeOptional(command.contactValue(), 128));
        audit.setStatus(SpecialAuditStatus.PENDING_REVIEW);
        SpecialOrderAudit saved = specialOrderAuditRepository.save(audit);
        List<SpecialOrderAuditItem> items = auditItems(command.items(), command.orderType(), app, regionCode);
        specialOrderAuditItemRepository.saveAll(saved.getId(), items);
        saved.setItems(items);
        return saved;
    }

    @Transactional
    public AdminSpecialOrderSubmission submitAdminSpecialOrder(
            Long customerId,
            Long adminId,
            Long customerAppId,
            String regionCode,
            OrderType orderType,
            BigDecimal amount
    ) {
        return submitAdminSpecialOrder(customerId, adminId, customerAppId, regionCode, orderType, null, null, null, amount);
    }

    @Transactional
    public AdminSpecialOrderSubmission submitAdminSpecialOrder(
            Long customerId,
            Long adminId,
            Long customerAppId,
            String regionCode,
            OrderType orderType,
            List<SubmitSpecialAuditCommand.AuditItem> items,
            BigDecimal amount
    ) {
        return submitAdminSpecialOrder(customerId, adminId, customerAppId, regionCode, orderType, items, null, null, amount);
    }

    @Transactional
    public AdminSpecialOrderSubmission submitAdminSpecialOrder(
            Long customerId,
            Long adminId,
            Long customerAppId,
            String regionCode,
            OrderType orderType,
            List<SubmitSpecialAuditCommand.AuditItem> items,
            String contactType,
            String contactValue,
            BigDecimal amount
    ) {
        if (customerId == null || customerAppId == null || orderType == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        if (!isSpecialOrderType(orderType)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.SPECIAL_AUDIT_PRICE_INVALID);
        }

        CustomerApp app = customerAppRepository.findById(customerAppId)
                .filter(candidate -> customerId.equals(candidate.getCustomerId()))
                .filter(candidate -> CustomerAppStatus.ACTIVE.equals(candidate.getStatus()))
                .orElseThrow(() -> new BusinessException(ErrorCode.APP_NOT_VERIFIED));
        String resolvedRegionCode = resolveLinkedRegion(app, regionCode);
        String content = defaultAdminSpecialContent(orderType, app);

        SpecialOrderAudit audit = new SpecialOrderAudit();
        audit.setAuditNo(generateAuditNo());
        audit.setCustomerId(customerId);
        audit.setCustomerAppId(app.getId());
        audit.setOrderType(orderType);
        audit.setStoreType(app.getStoreType());
        audit.setRegionCode(resolvedRegionCode);
        audit.setAppIdentifier(app.getAppIdentifier());
        audit.setAppName(app.getAppName());
        audit.setAppIconUrl(app.getAppIconUrl());
        audit.setRequestedContent(content);
        audit.setContactType(normalizeOptional(contactType, 32));
        audit.setContactValue(normalizeOptional(contactValue, 128));
        audit.setNegotiatedContent(content);
        audit.setNegotiatedPrice(amount);
        audit.setStatus(SpecialAuditStatus.APPROVED_WAIT_SUBMIT);
        audit.setReviewedByAdminId(adminId);
        audit.setReviewedAt(now());
        SpecialOrderAudit saved = specialOrderAuditRepository.save(audit);
        List<SpecialOrderAuditItem> auditItems = auditItems(items, orderType, app, resolvedRegionCode);
        specialOrderAuditItemRepository.saveAll(saved.getId(), auditItems);
        saved.setItems(auditItems);

        try {
            AsoOrder order = submitApprovedAudit(customerId, saved.getId());
            return new AdminSpecialOrderSubmission(order, saved, true);
        } catch (BusinessException exception) {
            if (ErrorCode.BALANCE_NOT_ENOUGH.equals(exception.getErrorCode())) {
                return new AdminSpecialOrderSubmission(null, saved, false);
            }
            throw exception;
        }
    }

    @Transactional
    public SpecialOrderAudit reviewAudit(Long auditId, Long adminId, ReviewSpecialAuditCommand command) {
        SpecialOrderAudit audit = specialOrderAuditRepository.findById(auditId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SPECIAL_AUDIT_NOT_FOUND));
        if (!SpecialAuditStatus.PENDING_REVIEW.equals(audit.getStatus())) {
            throw new BusinessException(ErrorCode.SPECIAL_AUDIT_STATUS_INVALID);
        }
        if (command == null || command.negotiatedPrice() == null) {
            throw new BusinessException(ErrorCode.SPECIAL_AUDIT_PRICE_REQUIRED);
        }
        if (command.negotiatedPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.SPECIAL_AUDIT_PRICE_INVALID);
        }

        audit.setNegotiatedContent(normalizeOrFallback(command.negotiatedContent(), audit.getRequestedContent()));
        audit.setNegotiatedPrice(command.negotiatedPrice());
        audit.setStatus(SpecialAuditStatus.APPROVED_WAIT_SUBMIT);
        audit.setReviewedByAdminId(adminId);
        audit.setReviewedAt(now());
        return specialOrderAuditRepository.update(audit);
    }

    @Transactional
    public SpecialOrderAudit cancelAudit(Long auditId, Long adminId, String reason) {
        SpecialOrderAudit audit = specialOrderAuditRepository.findById(auditId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SPECIAL_AUDIT_NOT_FOUND));
        if (SpecialAuditStatus.SUBMITTED.equals(audit.getStatus())) {
            throw new BusinessException(ErrorCode.SPECIAL_AUDIT_LOCKED);
        }
        String cancelReason = normalizeRequired(reason, ErrorCode.SPECIAL_AUDIT_CANCEL_REASON_REQUIRED);

        audit.setStatus(SpecialAuditStatus.CANCELLED);
        audit.setCancelReason(cancelReason);
        audit.setReviewedByAdminId(adminId);
        audit.setReviewedAt(now());
        return specialOrderAuditRepository.update(audit);
    }

    @Transactional
    public AsoOrder submitApprovedAudit(Long customerId, Long auditId) {
        SpecialOrderAudit audit = specialOrderAuditRepository.findById(auditId)
                .filter(candidate -> customerId.equals(candidate.getCustomerId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.SPECIAL_AUDIT_NOT_FOUND));
        if (!SpecialAuditStatus.APPROVED_WAIT_SUBMIT.equals(audit.getStatus())) {
            throw new BusinessException(ErrorCode.SPECIAL_AUDIT_STATUS_INVALID);
        }
        if (audit.getNegotiatedPrice() == null || audit.getNegotiatedPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.SPECIAL_AUDIT_PRICE_REQUIRED);
        }

        WalletDebitResult debit = walletService.debitForOrder(
                customerId,
                audit.getNegotiatedPrice(),
                "ORDER_DEDUCT:" + audit.getOrderType().name() + ":AUDIT:" + audit.getAuditNo()
        );

        LocalDate orderDate = LocalDate.ofInstant(clock.instant(), BUSINESS_ZONE);
        AsoOrder order = new AsoOrder();
        order.setOrderNo(generateOrderNo());
        order.setCustomerId(customerId);
        order.setCustomerAppId(audit.getCustomerAppId());
        order.setSourceAuditId(audit.getId());
        order.setOrderType(audit.getOrderType());
        order.setStoreType(audit.getStoreType());
        order.setRegionCode(audit.getRegionCode());
        order.setAppIdentifier(audit.getAppIdentifier());
        order.setAppName(audit.getAppName());
        order.setAppIconUrl(audit.getAppIconUrl());
        order.setStatus(OrderStatus.PENDING_CONFIRM);
        order.setOrderStartDate(orderDate);
        order.setOrderEndDate(orderDate);
        order.setTotalDays(1);
        order.setQuantity(1);
        order.setUnitPrice(audit.getNegotiatedPrice());
        order.setTotalAmount(audit.getNegotiatedPrice());
        order.setBalanceBefore(debit.balanceBefore());
        order.setBalanceAfter(debit.balanceAfter());
        order.setDeductedTransactionId(debit.transactionId());
        order.setExpectedCompletedAt(orderDate.plusDays(1).atStartOfDay());

        AsoOrder saved = orderRepository.save(order);
        OrderItem item = specialOrderItem(audit);
        orderItemRepository.saveAll(saved.getId(), List.of(item));
        saved.setItems(List.of(item));

        audit.setStatus(SpecialAuditStatus.SUBMITTED);
        audit.setSubmittedOrderId(saved.getId());
        audit.setSubmittedAt(now());
        specialOrderAuditRepository.update(audit);
        notifyOrderCreated(saved);
        return saved;
    }

    private void notifyOrderCreated(AsoOrder order) {
        Runnable notification = () -> {
            try {
                orderNotificationSender.notifyOrderCreated(order);
            } catch (RuntimeException exception) {
                log.warn("Order notification failed after special order committed. orderNo={}, errorType={}",
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

    public List<SpecialOrderAudit> listCustomerAudits(Long customerId) {
        if (customerId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        return attachItems(specialOrderAuditRepository.findByCustomerId(customerId));
    }

    public PageResult<SpecialOrderAudit> pageCustomerAudits(Long customerId, Integer page, Integer pageSize) {
        if (customerId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        Page normalized = normalizePage(page, pageSize);
        List<SpecialOrderAudit> audits = attachItems(specialOrderAuditRepository.findByCustomerId(
                customerId,
                normalized.pageSize(),
                normalized.offset()
        ));
        long total = specialOrderAuditRepository.countByCustomerId(customerId);
        return new PageResult<>(audits, normalized.page(), normalized.pageSize(), total);
    }

    public List<SpecialOrderAudit> listAdminAudits() {
        return attachItems(specialOrderAuditRepository.findAll());
    }

    public PageResult<SpecialOrderAudit> pageAdminAudits(Integer page, Integer pageSize) {
        Page normalized = normalizePage(page, pageSize);
        List<SpecialOrderAudit> audits = attachItems(specialOrderAuditRepository.findAll(
                normalized.pageSize(),
                normalized.offset()
        ));
        long total = specialOrderAuditRepository.countAll();
        return new PageResult<>(audits, normalized.page(), normalized.pageSize(), total);
    }

    private Page normalizePage(Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        return new Page(safePage, safePageSize);
    }

    private record Page(int page, int pageSize) {
        int offset() {
            return (page - 1) * pageSize;
        }
    }

    private OrderItem specialOrderItem(SpecialOrderAudit audit) {
        OrderItem item = new OrderItem();
        item.setItemType(audit.getOrderType().name());
        item.setItemName(normalizeOrFallback(audit.getNegotiatedContent(), audit.getRequestedContent()));
        item.setQuantity(1);
        item.setUnitPrice(audit.getNegotiatedPrice());
        item.setAmount(audit.getNegotiatedPrice());
        item.setMetadataJson("{\"sourceAuditId\":" + audit.getId() + "}");
        return item;
    }

    private List<SpecialOrderAudit> attachItems(List<SpecialOrderAudit> audits) {
        if (audits.isEmpty()) {
            return audits;
        }
        List<Long> auditIds = audits.stream().map(SpecialOrderAudit::getId).toList();
        Map<Long, List<SpecialOrderAuditItem>> itemMap = specialOrderAuditItemRepository.findByAuditIds(auditIds);
        audits.forEach(audit -> audit.setItems(itemMap.getOrDefault(audit.getId(), List.of())));
        return audits;
    }

    private List<SpecialOrderAuditItem> auditItems(
            List<SubmitSpecialAuditCommand.AuditItem> items,
            OrderType orderType,
            CustomerApp app,
            String fallbackRegionCode
    ) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
                .filter(item -> item != null)
                .map(item -> {
                    String keyword = normalizeLength(item.keyword(), 255, true);
                    Integer targetRank = item.targetRank();
                    if (orderType == OrderType.RANK_GUARANTEE && (targetRank == null || targetRank <= 0)) {
                        throw new BusinessException(ErrorCode.BAD_REQUEST);
                    }
                    SpecialOrderAuditItem result = new SpecialOrderAuditItem();
                    String requestedRegion = item.regionCode() == null || item.regionCode().isBlank()
                            ? fallbackRegionCode
                            : item.regionCode();
                    result.setRegionCode(resolveLinkedRegion(app, requestedRegion));
                    result.setKeyword(keyword);
                    result.setTargetRank(orderType == OrderType.RANK_GUARANTEE ? targetRank : null);
                    result.setCoverageNote(normalizeLength(item.coverageNote(), 500, false));
                    return result;
                })
                .toList();
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

    private boolean isSpecialOrderType(OrderType orderType) {
        return orderType == OrderType.RANK_GUARANTEE || orderType == OrderType.KEYWORD_COVERAGE;
    }

    private String defaultAdminSpecialContent(OrderType orderType, CustomerApp app) {
        String serviceName = orderType == OrderType.RANK_GUARANTEE ? "关键词保排名服务" : "关键词覆盖服务";
        return serviceName + " - " + app.getAppName();
    }

    private String normalizeRequired(String value, ErrorCode errorCode) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(errorCode);
        }
        return value.trim();
    }

    private String normalizeOptional(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        return trimmed;
    }

    private String normalizeLength(String value, int maxLength, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new BusinessException(ErrorCode.BAD_REQUEST);
            }
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        return normalized;
    }

    private String normalizeOrFallback(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback == null ? "" : fallback.trim();
        }
        return value.trim();
    }

    private String generateAuditNo() {
        return BusinessNumberGenerator.generate("SA", now());
    }

    private String generateOrderNo() {
        return BusinessNumberGenerator.generate("YO", now());
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(clock.instant(), BUSINESS_ZONE);
    }

    public record AdminSpecialOrderSubmission(AsoOrder order, SpecialOrderAudit audit, boolean paid) {
    }
}
