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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

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
import java.util.stream.Stream;

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
            CLOCK,
            org.mockito.Mockito.mock(com.youou.aso.modules.order.repository.OrderEventRepository.class)
    );

    @Test void specialAuditHonorsModuleStoresAndCountries() {
        var modules=org.mockito.Mockito.mock(com.youou.aso.modules.pricing.service.OrderModuleConfigService.class);
        var pricing=org.mockito.Mockito.mock(com.youou.aso.modules.pricing.repository.PricingConfigRepository.class);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"moduleService",modules);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"modulePricing",pricing);
        var module=new com.youou.aso.modules.pricing.domain.OrderModuleConfig(55L,"a","a","a","a","a","d","d","d","d","d",OrderType.KEYWORD_COVERAGE,null,null,true,1,List.of(StoreType.GOOGLE_PLAY));
        org.mockito.Mockito.when(modules.requireEnabled(55L,OrderType.KEYWORD_COVERAGE)).thenReturn(module);
        org.mockito.Mockito.when(pricing.findModuleRegions(55L)).thenReturn(List.of("US"));
        var command=new SubmitSpecialAuditCommand(1L,"US",OrderType.KEYWORD_COVERAGE,"keywords",null,null,List.of(new SubmitSpecialAuditCommand.AuditItem("US","keyword",null,null)),55L);
        assertThatThrownBy(() -> service.submitCustomerAudit(10L,command)).isInstanceOf(BusinessException.class).extracting("errorCode").isEqualTo(ErrorCode.STORE_REGION_NOT_SUPPORTED);
        module=new com.youou.aso.modules.pricing.domain.OrderModuleConfig(55L,"a","a","a","a","a","d","d","d","d","d",OrderType.KEYWORD_COVERAGE,null,null,true,1,List.of(StoreType.APP_STORE));
        org.mockito.Mockito.when(modules.requireEnabled(55L,OrderType.KEYWORD_COVERAGE)).thenReturn(module);
        org.mockito.Mockito.when(pricing.findModuleRegions(55L)).thenReturn(List.of("JP"));
        assertThatThrownBy(() -> service.submitCustomerAudit(10L,command)).isInstanceOf(BusinessException.class).extracting("errorCode").isEqualTo(ErrorCode.STORE_REGION_NOT_SUPPORTED);
        org.mockito.Mockito.when(pricing.findModuleRegions(55L)).thenReturn(List.of("US"));
        assertThat(service.submitCustomerAudit(10L,command).getOrderModuleId()).isEqualTo(55L);
        assertThatThrownBy(() -> service.submitCustomerAudit(10L,new SubmitSpecialAuditCommand(1L,"US",OrderType.KEYWORD_COVERAGE,"keywords",null,null,List.of(),55L))).isInstanceOf(BusinessException.class);
    }

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

    @ParameterizedTest
    @EnumSource(value = OrderType.class, names = {"RANK_GUARANTEE", "CHART_RANK_GUARANTEE", "KEYWORD_COVERAGE"})
    void adminEditsSpecialDetailsAndAdjustsAmountAndDuration(OrderType type) {
        for (OrderStatus status : List.of(OrderStatus.PENDING_CONFIRM, OrderStatus.PENDING_EXECUTION)) {
            walletService.balance = new BigDecimal("100.00");
            AsoOrder order = service.submitAdminSpecialOrder(10L, 7L, 1L, "US", type,
                    List.of(pricedEditItem("US", "original", "2.00", 2)), null).order();
            order.setStatus(status);
            Long auditId = order.getSourceAuditId();
            AsoOrder edited = service.editSubmittedOrder(order.getId(), 10L, 7L, 1L, type,
                    List.of(pricedEditItem("JP", "changed", "3.00", 3), pricedEditItem("US", "second", "1.00", 1)));
            assertThat(edited.getStatus()).isEqualTo(status);
            assertThat(edited.getSourceAuditId()).isEqualTo(auditId);
            assertThat(edited.getTotalAmount()).isEqualByComparingTo("10.00");
            assertThat(walletService.balance).isEqualByComparingTo("90.00");
            assertThat(edited.getTotalDays()).isEqualTo(3);
            assertThat(edited.getExpectedCompletedAt()).isEqualTo(LocalDateTime.of(2026, 6, 23, 0, 0));
            assertThat(auditItemRepository.findByAuditIds(List.of(auditId)).get(auditId))
                    .extracting(SpecialOrderAuditItem::getKeyword).containsExactly("changed", "second");
            assertThat(auditRepository.findById(auditId).orElseThrow().getNegotiatedPrice()).isEqualByComparingTo("10.00");
            service.editSubmittedOrder(order.getId(), 10L, 7L, 1L, type, List.of(pricedEditItem("US", "final", "1.00", 1)));
            assertThat(walletService.balance).isEqualByComparingTo("99.00");
            assertThat(edited.getStatus()).isEqualTo(status);
            assertThat(edited.getRegionCode()).isEqualTo("US");
        }
    }

    @Test
    void specialEditInsufficientBalanceAndInvalidStateDoNotChangeDetails() {
        walletService.balance = new BigDecimal("4.00");
        AsoOrder order = service.submitAdminSpecialOrder(10L, 7L, 1L, "US", OrderType.KEYWORD_COVERAGE,
                List.of(pricedEditItem("US", "original", "2.00", 2)), null).order();
        assertThatThrownBy(() -> service.editSubmittedOrder(order.getId(), 10L, 7L, 1L, order.getOrderType(),
                List.of(pricedEditItem("US", "changed", "3.00", 3))))
                .isInstanceOf(BusinessException.class).hasMessage(ErrorCode.BALANCE_NOT_ENOUGH.name());
        assertThat(order.getTotalAmount()).isEqualByComparingTo("4.00");
        assertThat(auditItemRepository.findByAuditIds(List.of(order.getSourceAuditId())).get(order.getSourceAuditId()).get(0).getKeyword()).isEqualTo("original");
        order.setStatus(OrderStatus.EXECUTING);
        assertThatThrownBy(() -> service.editSubmittedOrder(order.getId(), 10L, 7L, 1L, order.getOrderType(),
                List.of(pricedEditItem("US", "changed", "1.00", 1))))
                .isInstanceOf(BusinessException.class).hasMessage(ErrorCode.ORDER_STATUS_INVALID.name());
    }

    private SubmitSpecialAuditCommand.AuditItem pricedEditItem(String region, String keyword, String price, int days) {
        return new SubmitSpecialAuditCommand.AuditItem(region, keyword, keyword, 5, null, new BigDecimal(price), days);
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
    void customerCoverageAuditStillAcceptsUnpricedKeywordsWithoutRankingOrCharging() {
        walletService.balance = new BigDecimal("100.00");

        SpecialOrderAudit audit = unpricedCoverageAudit();

        assertThat(audit.getStatus()).isEqualTo(SpecialAuditStatus.PENDING_REVIEW);
        assertThat(audit.getNegotiatedPrice()).isNull();
        assertThat(audit.getItems()).hasSize(2).allSatisfy(item -> {
            assertThat(item.getTargetRank()).isNull();
            assertThat(item.getUnitPrice()).isNull();
            assertThat(item.getExecutionDays()).isNull();
        });
        assertThat(walletService.balance).isEqualByComparingTo("100.00");
        assertThat(orderRepository.saved).isEmpty();
    }

    @Test
    void adminCoverageOrderUsesItemPricingForSavedAuditOrderAndWalletInsteadOfClientTotal() {
        walletService.balance = new BigDecimal("100.00");

        SpecialOrderAuditService.AdminSpecialOrderSubmission result = service.submitAdminSpecialOrder(
                10L, 7L, 1L, "US", OrderType.KEYWORD_COVERAGE,
                List.of(
                        new SubmitSpecialAuditCommand.AuditItem("US", "chat app", null, null, null, new BigDecimal("1.25"), 10),
                        new SubmitSpecialAuditCommand.AuditItem("JP", "assistant", null, 99, null, new BigDecimal("2.10"), 5)),
                new BigDecimal("0.01"));

        assertThat(result.paid()).isTrue();
        assertThat(result.audit().getNegotiatedPrice()).isEqualByComparingTo("23.00");
        assertThat(result.audit().getItems())
                .extracting(SpecialOrderAuditItem::getRegionCode, SpecialOrderAuditItem::getKeyword,
                        SpecialOrderAuditItem::getTargetRank, SpecialOrderAuditItem::getUnitPrice,
                        SpecialOrderAuditItem::getExecutionDays)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("US", "chat app", null, new BigDecimal("1.25"), 10),
                        org.assertj.core.groups.Tuple.tuple("JP", "assistant", null, new BigDecimal("2.10"), 5));
        assertThat(auditItemRepository.itemsByAuditId.get(result.audit().getId())).hasSize(2);
        assertThat(result.order().getOrderType()).isEqualTo(OrderType.KEYWORD_COVERAGE);
        assertThat(result.order().getStatus()).isEqualTo(OrderStatus.PENDING_EXECUTION);
        assertThat(result.order().getTotalAmount()).isEqualByComparingTo("23.00");
        assertThat(result.order().getBalanceAfter()).isEqualByComparingTo("77.00");
        assertThat(orderItemRepository.itemsByOrderId.get(result.order().getId()))
                .singleElement().satisfies(item -> assertThat(item.getAmount()).isEqualByComparingTo("23.00"));
        assertThat(walletService.balance).isEqualByComparingTo("77.00");
    }

    @Test
    void adminCoverageOrderKeepsCalculatedQuoteWhenBalanceIsInsufficient() {
        walletService.balance = new BigDecimal("10.00");

        SpecialOrderAuditService.AdminSpecialOrderSubmission result = service.submitAdminSpecialOrder(
                10L, 7L, 1L, "US", OrderType.KEYWORD_COVERAGE,
                List.of(coverageItem(new BigDecimal("2.50"), 5)), new BigDecimal("0.01"));

        assertThat(result.paid()).isFalse();
        assertThat(result.order()).isNull();
        assertThat(result.audit().getStatus()).isEqualTo(SpecialAuditStatus.APPROVED_WAIT_SUBMIT);
        assertThat(result.audit().getNegotiatedPrice()).isEqualByComparingTo("12.50");
        assertThat(result.audit().getItems()).singleElement().satisfies(item -> {
            assertThat(item.getUnitPrice()).isEqualByComparingTo("2.50");
            assertThat(item.getExecutionDays()).isEqualTo(5);
        });
        assertThat(orderRepository.saved).isEmpty();
        assertThat(walletService.balance).isEqualByComparingTo("10.00");
    }

    @ParameterizedTest
    @MethodSource("invalidItemPricing")
    void adminCoverageOrderRejectsMissingOrInvalidItemPriceAndDays(BigDecimal unitPrice, Integer executionDays) {
        walletService.balance = new BigDecimal("100.00");

        assertThatThrownBy(() -> service.submitAdminSpecialOrder(
                10L, 7L, 1L, "US", OrderType.KEYWORD_COVERAGE,
                List.of(coverageItem(unitPrice, executionDays)), new BigDecimal("88.00")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.SPECIAL_AUDIT_PRICE_INVALID);

        assertThat(auditRepository.findAll()).isEmpty();
        assertThat(orderRepository.saved).isEmpty();
        assertThat(walletService.balance).isEqualByComparingTo("100.00");
    }

    @Test
    void adminCoverageOrderRequiresKeywordItemsEvenWhenClientProvidesGlobalPrice() {
        assertThatThrownBy(() -> service.submitAdminSpecialOrder(
                10L, 7L, 1L, "US", OrderType.KEYWORD_COVERAGE, List.of(), new BigDecimal("88.00")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.SPECIAL_AUDIT_PRICE_REQUIRED);
        assertThat(auditRepository.findAll()).isEmpty();
    }

    @Test
    void coverageReviewPersistsPerKeywordPricingAndChargesCalculatedTotalOnSubmission() {
        walletService.balance = new BigDecimal("100.00");
        SpecialOrderAudit audit = unpricedCoverageAudit();

        SpecialOrderAudit approved = service.reviewAudit(audit.getId(), 7L, new ReviewSpecialAuditCommand(
                "Coverage approved", new BigDecimal("0.01"), List.of(
                new ReviewSpecialAuditCommand.ItemPricing(2L, new BigDecimal("2.10"), 5),
                new ReviewSpecialAuditCommand.ItemPricing(1L, new BigDecimal("1.25"), 10))));

        assertThat(approved.getStatus()).isEqualTo(SpecialAuditStatus.APPROVED_WAIT_SUBMIT);
        assertThat(approved.getNegotiatedPrice()).isEqualByComparingTo("23.00");
        assertThat(approved.getItems()).allSatisfy(item -> assertThat(item.getTargetRank()).isNull());
        assertThat(approved.getItems())
                .extracting(SpecialOrderAuditItem::getUnitPrice, SpecialOrderAuditItem::getExecutionDays)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(new BigDecimal("1.25"), 10),
                        org.assertj.core.groups.Tuple.tuple(new BigDecimal("2.10"), 5));
        assertThat(walletService.balance).isEqualByComparingTo("100.00");

        AsoOrder order = service.submitApprovedAudit(10L, audit.getId());

        assertThat(order.getTotalAmount()).isEqualByComparingTo("23.00");
        assertThat(order.getItems()).singleElement()
                .satisfies(item -> assertThat(item.getAmount()).isEqualByComparingTo("23.00"));
        assertThat(walletService.balance).isEqualByComparingTo("77.00");
    }

    @ParameterizedTest
    @MethodSource("incompleteReviewPricing")
    void coverageReviewRequiresPricesForExactlyItsOwnKeywords(
            List<ReviewSpecialAuditCommand.ItemPricing> pricing, ErrorCode expectedError) {
        SpecialOrderAudit audit = unpricedCoverageAudit();

        assertThatThrownBy(() -> service.reviewAudit(audit.getId(), 7L,
                new ReviewSpecialAuditCommand("Approved", new BigDecimal("88.00"), pricing)))
                .isInstanceOf(BusinessException.class).extracting("errorCode").isEqualTo(expectedError);

        assertThat(audit.getStatus()).isEqualTo(SpecialAuditStatus.PENDING_REVIEW);
        assertThat(audit.getNegotiatedPrice()).isNull();
        assertThat(auditItemRepository.itemsByAuditId.get(audit.getId()))
                .allSatisfy(item -> assertThat(item.getUnitPrice()).isNull());
    }

    @ParameterizedTest
    @MethodSource("invalidItemPricing")
    void coverageReviewValidatesEveryPriceBeforeUpdatingAnyKeyword(BigDecimal unitPrice, Integer executionDays) {
        SpecialOrderAudit audit = unpricedCoverageAudit();

        assertThatThrownBy(() -> service.reviewAudit(audit.getId(), 7L,
                new ReviewSpecialAuditCommand("Approved", new BigDecimal("88.00"), List.of(
                        new ReviewSpecialAuditCommand.ItemPricing(1L, new BigDecimal("1.25"), 10),
                        new ReviewSpecialAuditCommand.ItemPricing(2L, unitPrice, executionDays)))))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.SPECIAL_AUDIT_PRICE_INVALID);

        assertThat(audit.getStatus()).isEqualTo(SpecialAuditStatus.PENDING_REVIEW);
        assertThat(auditItemRepository.itemsByAuditId.get(audit.getId())).allSatisfy(item -> {
            assertThat(item.getUnitPrice()).isNull();
            assertThat(item.getExecutionDays()).isNull();
        });
    }

    @Test
    void legacyCoverageAuditWithoutItemsStillAcceptsOverallQuote() {
        SpecialOrderAudit audit = pendingAudit();
        audit.setOrderType(OrderType.KEYWORD_COVERAGE);
        auditRepository.save(audit);

        SpecialOrderAudit approved = service.reviewAudit(audit.getId(), 7L,
                new ReviewSpecialAuditCommand("Legacy coverage package", new BigDecimal("88.00")));

        assertThat(approved.getStatus()).isEqualTo(SpecialAuditStatus.APPROVED_WAIT_SUBMIT);
        assertThat(approved.getNegotiatedPrice()).isEqualByComparingTo("88.00");
    }

    @Test
    void previouslyApprovedCoveragePackageWithUnpricedKeywordsRetainsItsAgreedPrice() {
        walletService.balance = new BigDecimal("100.00");
        SpecialOrderAudit audit = unpricedCoverageAudit();
        audit.setStatus(SpecialAuditStatus.APPROVED_WAIT_SUBMIT);
        audit.setNegotiatedPrice(new BigDecimal("88.00"));

        AsoOrder order = service.submitApprovedAudit(10L, audit.getId());

        assertThat(order.getTotalAmount()).isEqualByComparingTo("88.00");
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getTotalDays()).isEqualTo(1);
        assertThat(order.getOrderEndDate()).isEqualTo(LocalDate.of(2026, 6, 20));
        assertThat(order.getExpectedCompletedAt()).isEqualTo(LocalDate.of(2026, 6, 21).atStartOfDay());
        assertThat(walletService.balance).isEqualByComparingTo("12.00");
    }

    @ParameterizedTest
    @EnumSource(value = OrderType.class, names = {"RANK_GUARANTEE", "CHART_RANK_GUARANTEE", "KEYWORD_COVERAGE"})
    void customerSubmissionUsesLongestPersistedReviewedItemDuration(OrderType orderType) {
        walletService.balance = new BigDecimal("100.00");
        SpecialOrderAudit audit = service.submitCustomerAudit(10L, new SubmitSpecialAuditCommand(
                1L, "US", orderType, "Keep the requested service running", List.of(
                new SubmitSpecialAuditCommand.AuditItem("US", "chat app", "Top Free", 3, null),
                new SubmitSpecialAuditCommand.AuditItem("JP", "assistant", "Top Paid", 5, null))));
        service.reviewAudit(audit.getId(), 7L, new ReviewSpecialAuditCommand(
                "Approved service", null, List.of(
                new ReviewSpecialAuditCommand.ItemPricing(1L, new BigDecimal("2.00"), 5),
                new ReviewSpecialAuditCommand.ItemPricing(2L, new BigDecimal("1.00"), 10))));
        // A fresh repository read does not hydrate the audit's transient item list.
        audit.setItems(List.of());

        AsoOrder order = service.submitApprovedAudit(10L, audit.getId());

        assertThat(order.getOrderStartDate()).isEqualTo(LocalDate.of(2026, 6, 20));
        assertThat(order.getOrderEndDate()).isEqualTo(LocalDate.of(2026, 6, 29));
        assertThat(order.getTotalDays()).isEqualTo(10);
        assertThat(order.getExpectedCompletedAt()).isEqualTo(LocalDate.of(2026, 6, 30).atStartOfDay());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_CONFIRM);
        assertThat(order.getQuantity()).isEqualTo(1);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("20.00");
        assertThat(order.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getQuantity()).isEqualTo(1);
            assertThat(item.getAmount()).isEqualByComparingTo("20.00");
        });
        assertThat(walletService.balance).isEqualByComparingTo("80.00");
    }

    @ParameterizedTest
    @EnumSource(value = OrderType.class, names = {"RANK_GUARANTEE", "CHART_RANK_GUARANTEE", "KEYWORD_COVERAGE"})
    void adminDirectSubmissionUsesLongestItemDuration(OrderType orderType) {
        walletService.balance = new BigDecimal("100.00");

        SpecialOrderAuditService.AdminSpecialOrderSubmission result = service.submitAdminSpecialOrder(
                10L, 7L, 1L, "US", orderType, List.of(
                new SubmitSpecialAuditCommand.AuditItem("US", "chat app", "Top Free", 3,
                        null, new BigDecimal("2.00"), 7),
                new SubmitSpecialAuditCommand.AuditItem("JP", "assistant", "Top Paid", 5,
                        null, new BigDecimal("1.00"), 3)), null);

        assertThat(result.paid()).isTrue();
        AsoOrder order = result.order();
        assertThat(order.getOrderStartDate()).isEqualTo(LocalDate.of(2026, 6, 20));
        assertThat(order.getOrderEndDate()).isEqualTo(LocalDate.of(2026, 6, 26));
        assertThat(order.getTotalDays()).isEqualTo(7);
        assertThat(order.getExpectedCompletedAt()).isEqualTo(LocalDate.of(2026, 6, 27).atStartOfDay());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_EXECUTION);
        assertThat(order.getQuantity()).isEqualTo(1);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("17.00");
        assertThat(walletService.balance).isEqualByComparingTo("83.00");
    }

    @Test
    void singleDaySpecialOrderStillCompletesAfterItsStartDate() {
        walletService.balance = new BigDecimal("100.00");

        AsoOrder order = service.submitAdminSpecialOrder(
                10L, 7L, 1L, "US", OrderType.KEYWORD_COVERAGE,
                List.of(coverageItem(new BigDecimal("2.50"), 1)), null).order();

        assertThat(order.getOrderStartDate()).isEqualTo(LocalDate.of(2026, 6, 20));
        assertThat(order.getOrderEndDate()).isEqualTo(LocalDate.of(2026, 6, 20));
        assertThat(order.getTotalDays()).isEqualTo(1);
        assertThat(order.getExpectedCompletedAt()).isEqualTo(LocalDate.of(2026, 6, 21).atStartOfDay());
    }

    @ParameterizedTest
    @EnumSource(value = OrderType.class, names = {"RANK_GUARANTEE", "CHART_RANK_GUARANTEE"})
    void rankedOrdersStillRequireTargetRankWhenItemPricesAreValid(OrderType orderType) {
        assertThatThrownBy(() -> service.submitAdminSpecialOrder(
                10L, 7L, 1L, "US", orderType,
                List.of(new SubmitSpecialAuditCommand.AuditItem("US", "chat app", "Top Free", null,
                        null, new BigDecimal("1.25"), 10)), new BigDecimal("88.00")))
                .isInstanceOf(BusinessException.class).extracting("errorCode").isEqualTo(ErrorCode.BAD_REQUEST);
    }

    private SpecialOrderAudit unpricedCoverageAudit() {
        return service.submitCustomerAudit(10L, new SubmitSpecialAuditCommand(
                1L, "US", OrderType.KEYWORD_COVERAGE, "Expand keyword coverage", List.of(
                new SubmitSpecialAuditCommand.AuditItem("US", "chat app", null, null),
                new SubmitSpecialAuditCommand.AuditItem("JP", "assistant", null, null))));
    }

    private static SubmitSpecialAuditCommand.AuditItem coverageItem(BigDecimal unitPrice, Integer executionDays) {
        return new SubmitSpecialAuditCommand.AuditItem("US", "chat app", null, null, null, unitPrice, executionDays);
    }

    private static Stream<Arguments> invalidItemPricing() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(null, 5),
                Arguments.of(new BigDecimal("1.25"), null),
                Arguments.of(BigDecimal.ZERO, 5),
                Arguments.of(new BigDecimal("-1.00"), 5),
                Arguments.of(new BigDecimal("1.255"), 5),
                Arguments.of(new BigDecimal("1.25"), 0),
                Arguments.of(new BigDecimal("1.25"), -1),
                Arguments.of(new BigDecimal("1.25"), 3651));
    }

    private static Stream<Arguments> incompleteReviewPricing() {
        ReviewSpecialAuditCommand.ItemPricing first = new ReviewSpecialAuditCommand.ItemPricing(1L, new BigDecimal("1.25"), 10);
        return Stream.of(
                Arguments.of(null, ErrorCode.SPECIAL_AUDIT_PRICE_REQUIRED),
                Arguments.of(List.of(first), ErrorCode.SPECIAL_AUDIT_PRICE_REQUIRED),
                Arguments.of(List.of(first, first), ErrorCode.SPECIAL_AUDIT_PRICE_INVALID),
                Arguments.of(List.of(first, new ReviewSpecialAuditCommand.ItemPricing(99L, new BigDecimal("2.00"), 5)),
                        ErrorCode.SPECIAL_AUDIT_PRICE_REQUIRED));
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
        assertThat(order.getOrderEndDate()).isEqualTo(LocalDate.of(2026, 6, 20));
        assertThat(order.getTotalDays()).isEqualTo(1);
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
        public void deleteByAuditId(Long auditId) {
            itemsByAuditId.remove(auditId);
        }

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
