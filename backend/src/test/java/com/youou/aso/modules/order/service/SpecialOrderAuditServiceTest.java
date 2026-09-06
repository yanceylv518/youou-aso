package com.youou.aso.modules.order.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.appmanagement.domain.CustomerApp;
import com.youou.aso.modules.appmanagement.domain.CustomerAppStatus;
import com.youou.aso.modules.appmanagement.domain.MarketRegion;
import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.appmanagement.dto.CustomerAppQuery;
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
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpecialOrderAuditServiceTest {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-20T00:00:00Z"), ZoneOffset.UTC);

    private final FakeCustomerAppRepository appRepository = new FakeCustomerAppRepository();
    private final FakeMarketRegionRepository marketRegionRepository = new FakeMarketRegionRepository();
    private final FakeSpecialOrderAuditRepository auditRepository = new FakeSpecialOrderAuditRepository();
    private final FakeSpecialOrderAuditItemRepository auditItemRepository = new FakeSpecialOrderAuditItemRepository();
    private final FakeOrderRepository orderRepository = new FakeOrderRepository();
    private final FakeOrderItemRepository orderItemRepository = new FakeOrderItemRepository();
    private final FakeWalletService walletService = new FakeWalletService();
    private final FakeOrderNotificationSender orderNotificationSender = new FakeOrderNotificationSender();
    private final SpecialOrderAuditService service = new SpecialOrderAuditService(
            appRepository,
            marketRegionRepository,
            auditRepository,
            auditItemRepository,
            orderRepository,
            orderItemRepository,
            walletService,
            orderNotificationSender,
            CLOCK
    );

    @Test
    void submitCustomerAuditCreatesPendingReviewWithoutChargingWallet() {
        walletService.balance = new BigDecimal("100.00");

        SpecialOrderAudit audit = service.submitCustomerAudit(10L, new SubmitSpecialAuditCommand(
                1L,
                OrderType.RANK_GUARANTEE,
                "Keep keyword rank for brand terms"
        ));

        assertThat(audit.getStatus()).isEqualTo(SpecialAuditStatus.PENDING_REVIEW);
        assertThat(audit.getAuditNo()).hasSize(14).matches("SA260620\\d{6}");
        assertThat(audit.getCustomerId()).isEqualTo(10L);
        assertThat(audit.getStoreType()).isEqualTo(StoreType.APP_STORE);
        assertThat(audit.getRequestedContent()).isEqualTo("Keep keyword rank for brand terms");
        assertThat(audit.getNegotiatedPrice()).isNull();
        assertThat(walletService.balance).isEqualByComparingTo("100.00");
    }

    @Test
    void submitCustomerAuditStoresStructuredKeywordItems() {
        SpecialOrderAudit audit = service.submitCustomerAudit(10L, new SubmitSpecialAuditCommand(
                1L,
                null,
                OrderType.RANK_GUARANTEE,
                "Keep rank for core keywords",
                List.of(
                        new SubmitSpecialAuditCommand.AuditItem("US", "chat app", 3, null),
                        new SubmitSpecialAuditCommand.AuditItem("JP", "ai assistant", 5, null)
                )
        ));

        assertThat(audit.getItems())
                .extracting(
                        SpecialOrderAuditItem::getRegionCode,
                        SpecialOrderAuditItem::getKeyword,
                        SpecialOrderAuditItem::getTargetRank,
                        SpecialOrderAuditItem::getCoverageNote
                )
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("US", "chat app", 3, null),
                        org.assertj.core.groups.Tuple.tuple("JP", "ai assistant", 5, null)
                );
        assertThat(auditItemRepository.itemsByAuditId.get(audit.getId())).hasSize(2);
    }

    @Test
    void submitChartRankGuaranteeStoresChartTypeInsteadOfKeywordInput() {
        SpecialOrderAudit audit = service.submitCustomerAudit(10L, new SubmitSpecialAuditCommand(
                1L,
                null,
                OrderType.CHART_RANK_GUARANTEE,
                "Keep chart position",
                List.of(new SubmitSpecialAuditCommand.AuditItem("US", null, "Top Free chart", 10, null))
        ));

        assertThat(audit.getOrderType()).isEqualTo(OrderType.CHART_RANK_GUARANTEE);
        assertThat(audit.getItems())
                .extracting(
                        SpecialOrderAuditItem::getRegionCode,
                        SpecialOrderAuditItem::getChartType,
                        SpecialOrderAuditItem::getTargetRank,
                        SpecialOrderAuditItem::getCoverageNote
                )
                .containsExactly(org.assertj.core.groups.Tuple.tuple("US", "Top Free chart", 10, null));
    }
    @Test
    void approveAuditLocksNegotiatedContentAndPrice() {
        SpecialOrderAudit audit = pendingAudit();
        auditRepository.save(audit);

        SpecialOrderAudit approved = service.reviewAudit(1L, 7L, new ReviewSpecialAuditCommand(
                "Final monthly guarantee package",
                new BigDecimal("88.00")
        ));

        assertThat(approved.getStatus()).isEqualTo(SpecialAuditStatus.APPROVED_WAIT_SUBMIT);
        assertThat(approved.getNegotiatedContent()).isEqualTo("Final monthly guarantee package");
        assertThat(approved.getNegotiatedPrice()).isEqualByComparingTo("88.00");
        assertThat(approved.getReviewedByAdminId()).isEqualTo(7L);
        assertThat(approved.getReviewedAt()).isEqualTo(expectedNow());
    }

    @Test
    void approveRankAuditCalculatesPriceFromEachItemsUnitPriceAndDays() {
        SpecialOrderAudit audit = service.submitCustomerAudit(10L, new SubmitSpecialAuditCommand(
                1L, null, OrderType.RANK_GUARANTEE, "Keep ranking", List.of(
                new SubmitSpecialAuditCommand.AuditItem("US", "chat", 3, null),
                new SubmitSpecialAuditCommand.AuditItem("US", "assistant", 5, null))));

        SpecialOrderAudit approved = service.reviewAudit(audit.getId(), 7L, new ReviewSpecialAuditCommand(
                "Approved", null, List.of(
                new ReviewSpecialAuditCommand.ItemPricing(1L, new BigDecimal("1.25"), 10),
                new ReviewSpecialAuditCommand.ItemPricing(2L, new BigDecimal("2.00"), 5))));

        assertThat(approved.getNegotiatedPrice()).isEqualByComparingTo("22.50");
        assertThat(auditItemRepository.itemsByAuditId.get(audit.getId()))
                .extracting(SpecialOrderAuditItem::getUnitPrice, SpecialOrderAuditItem::getExecutionDays)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(new BigDecimal("1.25"), 10),
                        org.assertj.core.groups.Tuple.tuple(new BigDecimal("2.00"), 5));
    }

    @Test
    void submitApprovedAuditCreatesFormalOrderAndDeductsBalance() {
        walletService.balance = new BigDecimal("100.00");
        SpecialOrderAudit audit = approvedAudit();
        auditRepository.save(audit);

        AsoOrder order = service.submitApprovedAudit(10L, 1L);

        assertThat(order.getSourceAuditId()).isEqualTo(1L);
        assertThat(order.getOrderNo()).matches("YO260620\\d{6}");
        assertThat(order.getOrderType()).isEqualTo(OrderType.RANK_GUARANTEE);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_CONFIRM);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("88.00");
        assertThat(order.getBalanceBefore()).isEqualByComparingTo("100.00");
        assertThat(order.getBalanceAfter()).isEqualByComparingTo("12.00");
        assertThat(order.getOrderStartDate()).isEqualTo(LocalDate.of(2026, 6, 20));
        assertThat(order.getExpectedCompletedAt()).isEqualTo(LocalDate.of(2026, 6, 21).atStartOfDay());
        assertThat(order.getItems())
                .extracting(OrderItem::getItemType, OrderItem::getItemName, OrderItem::getQuantity, OrderItem::getAmount)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(
                        "RANK_GUARANTEE",
                        "Final monthly guarantee package",
                        1,
                        new BigDecimal("88.00")
                ));
        assertThat(auditRepository.findById(1L).orElseThrow().getStatus()).isEqualTo(SpecialAuditStatus.SUBMITTED);
        assertThat(auditRepository.findById(1L).orElseThrow().getSubmittedOrderId()).isEqualTo(order.getId());
        assertThat(walletService.balance).isEqualByComparingTo("12.00");
        assertThat(orderNotificationSender.notifiedOrders).containsExactly(order);
    }

    @Test
    void submitApprovedAuditRejectsInsufficientBalanceWithoutCreatingOrder() {
        walletService.balance = new BigDecimal("10.00");
        auditRepository.save(approvedAudit());

        assertThatThrownBy(() -> service.submitApprovedAudit(10L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BALANCE_NOT_ENOUGH);
        assertThat(orderRepository.saved).isEmpty();
        assertThat(auditRepository.findById(1L).orElseThrow().getStatus()).isEqualTo(SpecialAuditStatus.APPROVED_WAIT_SUBMIT);
    }

    @Test
    void cancelAuditChangesAnyUnsubmittedAuditToCancelled() {
        auditRepository.save(approvedAudit());

        SpecialOrderAudit cancelled = service.cancelAudit(1L, 7L, "Customer no longer needs it");

        assertThat(cancelled.getStatus()).isEqualTo(SpecialAuditStatus.CANCELLED);
        assertThat(cancelled.getCancelReason()).isEqualTo("Customer no longer needs it");
        assertThat(cancelled.getReviewedByAdminId()).isEqualTo(7L);
    }

    private static LocalDateTime expectedNow() {
        return LocalDateTime.ofInstant(CLOCK.instant(), BUSINESS_ZONE);
    }

    private SpecialOrderAudit pendingAudit() {
        SpecialOrderAudit audit = new SpecialOrderAudit();
        audit.setId(1L);
        audit.setAuditNo("SA202606200001");
        audit.setCustomerId(10L);
        audit.setCustomerAppId(1L);
        audit.setOrderType(OrderType.RANK_GUARANTEE);
        audit.setStoreType(StoreType.APP_STORE);
        audit.setRegionCode("US");
        audit.setAppIdentifier("123456");
        audit.setAppName("Example App");
        audit.setAppIconUrl("https://example.test/icon.png");
        audit.setRequestedContent("Keep keyword rank for brand terms");
        audit.setStatus(SpecialAuditStatus.PENDING_REVIEW);
        return audit;
    }

    private SpecialOrderAudit approvedAudit() {
        SpecialOrderAudit audit = pendingAudit();
        audit.setStatus(SpecialAuditStatus.APPROVED_WAIT_SUBMIT);
        audit.setNegotiatedContent("Final monthly guarantee package");
        audit.setNegotiatedPrice(new BigDecimal("88.00"));
        return audit;
    }

    private static final class FakeCustomerAppRepository implements CustomerAppRepository {
        private final CustomerApp app;

        private FakeCustomerAppRepository() {
            app = new CustomerApp();
            app.setId(1L);
            app.setCustomerId(10L);
            app.setStoreType(StoreType.APP_STORE);
            app.setRegionCode("US");
            app.setAppIdentifier("123456");
            app.setAppName("Example App");
            app.setAppIconUrl("https://example.test/icon.png");
            app.setStatus(CustomerAppStatus.ACTIVE);
        }

        @Override
        public Optional<CustomerApp> findById(Long id) {
            return app.getId().equals(id) ? Optional.of(app) : Optional.empty();
        }

        @Override
        public boolean existsByCustomerAndStoreAndRegionAndIdentifier(Long customerId, StoreType storeType, String regionCode, String appIdentifier) {
            return false;
        }

        @Override
        public boolean existsByCustomerAndStoreAndRegionAndIdentifierExcludingId(Long customerId, StoreType storeType, String regionCode, String appIdentifier, Long excludedId) {
            return false;
        }

        @Override
        public CustomerApp save(CustomerApp app) {
            return app;
        }

        @Override
        public CustomerApp update(CustomerApp app) {
            return app;
        }

        @Override
        public void updateStatus(Long id, CustomerAppStatus status) {
        }

        @Override
        public List<CustomerApp> findByCustomerId(Long customerId, CustomerAppQuery query) {
            return List.of(app);
        }

        @Override
        public List<CustomerApp> findAll(CustomerAppQuery query) {
            return List.of(app);
        }
    }

    private static final class FakeMarketRegionRepository implements MarketRegionRepository {
        @Override
        public Optional<MarketRegion> findByCode(String code) {
            if (!List.of("US", "JP").contains(code)) {
                return Optional.empty();
            }
            MarketRegion region = new MarketRegion();
            region.setCode(code);
            region.setEnabled(true);
            region.setSupportsAppStore(true);
            region.setSupportsGooglePlay(false);
            region.setSupportsIpadStore(true);
            return Optional.of(region);
        }

        @Override
        public List<MarketRegion> findAll() {
            return List.of();
        }

        @Override
        public List<MarketRegion> findEnabled() {
            return List.of();
        }

        @Override
        public void update(MarketRegion region) {
        }
    }

    private static final class FakeSpecialOrderAuditRepository implements SpecialOrderAuditRepository {
        private final Map<Long, SpecialOrderAudit> audits = new java.util.HashMap<>();
        private long nextId = 1L;

        @Override
        public SpecialOrderAudit save(SpecialOrderAudit audit) {
            if (audit.getId() == null) {
                audit.setId(nextId++);
            }
            audits.put(audit.getId(), audit);
            return audit;
        }

        @Override
        public Optional<SpecialOrderAudit> findById(Long id) {
            return Optional.ofNullable(audits.get(id));
        }

        @Override
        public SpecialOrderAudit update(SpecialOrderAudit audit) {
            audits.put(audit.getId(), audit);
            return audit;
        }

        @Override
        public List<SpecialOrderAudit> findByCustomerId(Long customerId) {
            return audits.values().stream()
                    .filter(audit -> customerId.equals(audit.getCustomerId()))
                    .toList();
        }

        @Override
        public List<SpecialOrderAudit> findAll() {
            return List.copyOf(audits.values());
        }
    }

    private static final class FakeSpecialOrderAuditItemRepository implements SpecialOrderAuditItemRepository {
        private final Map<Long, List<SpecialOrderAuditItem>> itemsByAuditId = new java.util.HashMap<>();

        @Override
        public void saveAll(Long auditId, List<SpecialOrderAuditItem> items) {
            for (int index = 0; index < items.size(); index++) {
                items.get(index).setId((long) index + 1);
                items.get(index).setAuditId(auditId);
            }
            itemsByAuditId.put(auditId, new ArrayList<>(items));
        }

        @Override
        public Map<Long, List<SpecialOrderAuditItem>> findByAuditIds(List<Long> auditIds) {
            Map<Long, List<SpecialOrderAuditItem>> result = new java.util.HashMap<>();
            auditIds.forEach(auditId -> result.put(auditId, new ArrayList<>(itemsByAuditId.getOrDefault(auditId, List.of()))));
            return result;
        }

        @Override
        public void updatePricing(Long id, BigDecimal unitPrice, Integer executionDays) {
            itemsByAuditId.values().stream().flatMap(List::stream).filter(item -> id.equals(item.getId())).forEach(item -> {
                item.setUnitPrice(unitPrice);
                item.setExecutionDays(executionDays);
            });
        }
    }

    private static final class FakeOrderRepository implements OrderRepository {
        private final Map<Long, AsoOrder> orders = new java.util.HashMap<>();
        private final List<AsoOrder> saved = new ArrayList<>();
        private long nextId = 1L;

        @Override
        public AsoOrder save(AsoOrder order) {
            order.setId(nextId++);
            saved.add(order);
            orders.put(order.getId(), order);
            return order;
        }

        @Override
        public Optional<AsoOrder> findById(Long id) {
            return Optional.ofNullable(orders.get(id));
        }

        @Override
        public AsoOrder update(AsoOrder order) {
            orders.put(order.getId(), order);
            return order;
        }

        @Override
        public AsoOrder updatePaymentDraft(AsoOrder order) {
            orders.put(order.getId(), order);
            return order;
        }

        @Override
        public List<AsoOrder> findByCustomerId(Long customerId, com.youou.aso.modules.order.dto.OrderQuery query) {
            return List.of();
        }

        @Override
        public List<AsoOrder> findAll(com.youou.aso.modules.order.dto.OrderQuery query) {
            return List.of();
        }

        @Override
        public List<AsoOrder> findDueBefore(java.time.LocalDateTime now) {
            return List.of();
        }
    }

    private static final class FakeOrderItemRepository implements OrderItemRepository {
        private final Map<Long, List<OrderItem>> itemsByOrderId = new java.util.HashMap<>();

        @Override
        public void saveAll(Long orderId, List<OrderItem> items) {
            items.forEach(item -> item.setOrderId(orderId));
            itemsByOrderId.put(orderId, new ArrayList<>(items));
        }

        @Override
        public void deleteByOrderId(Long orderId) {
            itemsByOrderId.remove(orderId);
        }

        @Override
        public Map<Long, List<OrderItem>> findByOrderIds(List<Long> orderIds) {
            Map<Long, List<OrderItem>> result = new java.util.HashMap<>();
            orderIds.forEach(orderId -> result.put(orderId, new ArrayList<>(itemsByOrderId.getOrDefault(orderId, List.of()))));
            return result;
        }
    }

    private static final class FakeWalletService implements WalletService {
        private BigDecimal balance = BigDecimal.ZERO;
        private long nextTransactionId = 1L;

        @Override
        public WalletDebitResult debitForOrder(Long customerId, BigDecimal amount, String remark) {
            if (balance.compareTo(amount) < 0) {
                throw new BusinessException(ErrorCode.BALANCE_NOT_ENOUGH);
            }
            BigDecimal before = balance;
            balance = balance.subtract(amount);
            return new WalletDebitResult(nextTransactionId++, before, balance);
        }

        @Override
        public WalletDebitResult refundForOrder(Long customerId, BigDecimal amount, String remark) {
            BigDecimal before = balance;
            balance = balance.add(amount);
            return new WalletDebitResult(nextTransactionId++, before, balance);
        }

        @Override
        public BigDecimal currentBalanceForUpdate(Long customerId) {
            return balance;
        }
    }

    private static final class FakeOrderNotificationSender implements OrderNotificationSender {
        private final List<AsoOrder> notifiedOrders = new ArrayList<>();

        @Override
        public void notifyOrderCreated(AsoOrder order) {
            notifiedOrders.add(order);
        }
    }
}
