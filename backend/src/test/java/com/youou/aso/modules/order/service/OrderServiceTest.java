package com.youou.aso.modules.order.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountStatus;
import com.youou.aso.modules.account.domain.CustomerAccount;
import com.youou.aso.modules.account.repository.CustomerAccountRepository;
import com.youou.aso.modules.appmanagement.domain.CustomerApp;
import com.youou.aso.modules.appmanagement.domain.CustomerAppStatus;
import com.youou.aso.modules.appmanagement.domain.MarketRegion;
import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.appmanagement.dto.CustomerAppQuery;
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
import com.youou.aso.modules.pricing.repository.OrderModuleConfigRepository;
import com.youou.aso.modules.pricing.repository.PricingConfigRepository;
import com.youou.aso.modules.pricing.service.OrderModuleConfigService;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderServiceTest {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-20T00:00:00Z"), ZoneOffset.UTC);

    private final FakeCustomerAppRepository appRepository = new FakeCustomerAppRepository();
    private final FakeMarketRegionRepository marketRegionRepository = new FakeMarketRegionRepository();
    private final FakePricingConfigRepository pricingRepository = new FakePricingConfigRepository();
    private final FakeOrderModuleConfigRepository orderModuleRepository = new FakeOrderModuleConfigRepository();
    private final FakeCustomerAccountRepository customerAccountRepository = new FakeCustomerAccountRepository();
    private final FakeOrderRepository orderRepository = new FakeOrderRepository();
    private final FakeOrderItemRepository orderItemRepository = new FakeOrderItemRepository();
    private final FakeOrderEventRepository orderEventRepository = new FakeOrderEventRepository();
    private final FakeOrderCommentDetailRepository orderCommentDetailRepository = new FakeOrderCommentDetailRepository();
    private final FakeWalletService walletService = new FakeWalletService();
    private final FakeOrderNotificationSender orderNotificationSender = new FakeOrderNotificationSender();
    private final OrderService orderService = new OrderService(
            appRepository,
            marketRegionRepository,
            pricingRepository,
            new OrderModuleConfigService(orderModuleRepository),
            customerAccountRepository,
            orderRepository,
            orderItemRepository,
            orderEventRepository,
            orderCommentDetailRepository,
            walletService,
            orderNotificationSender,
            CLOCK,
            org.mockito.Mockito.mock(ReviewAttachmentService.class)
    );

    @Test
    void createKeywordInstallOrderChargesKeywordCountOnlyAndStartsPendingConfirm() {
        walletService.balance = new BigDecimal("100.00");

        AsoOrder order = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                OrderType.KEYWORD_INSTALL,
                LocalDate.of(2026, 6, 19),
                LocalDate.of(2026, 6, 21),
                8,
                List.of("chat app", "ai assistant", "productivity"),
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_CONFIRM);
        assertThat(order.getOrderNo()).matches("YO260620\\d{6}");
        assertThat(order.getStoreType()).isEqualTo(StoreType.APP_STORE);
        assertThat(order.getRegionCode()).isEqualTo("US");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("3.60");
        assertThat(order.getBalanceBefore()).isEqualByComparingTo("100.00");
        assertThat(order.getBalanceAfter()).isEqualByComparingTo("96.40");
        assertThat(order.getTotalDays()).isEqualTo(3);
        assertThat(order.getExpectedCompletedAt()).isEqualTo(LocalDate.of(2026, 6, 22).atStartOfDay());
        assertThat(walletService.balance).isEqualByComparingTo("96.40");
        assertThat(order.getItems())
                .extracting(OrderItem::getItemName)
                .containsExactly("chat app", "ai assistant", "productivity");
        assertThat(order.getItems())
                .extracting(OrderItem::getItemType, OrderItem::getQuantity, OrderItem::getUnitPrice)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("KEYWORD_INSTALL", 1, new BigDecimal("1.20")),
                        org.assertj.core.groups.Tuple.tuple("KEYWORD_INSTALL", 1, new BigDecimal("1.20")),
                        org.assertj.core.groups.Tuple.tuple("KEYWORD_INSTALL", 1, new BigDecimal("1.20"))
                );
        assertThat(orderNotificationSender.notifiedOrders).containsExactly(order);
        assertThat(order.getCustomerUsername()).isEqualTo("customer");
        assertThat(order.getCustomerEmail()).isEqualTo("customer@example.com");
    }

    @Test
    void adminCreatedOrderSkipsConfirmationAndStartsPendingExecution() {
        walletService.balance = new BigDecimal("100.00");

        AsoOrder order = orderService.createAdminOrderForCustomer(10L, 99L, new CreateOrderCommand(
                1L,
                OrderType.KEYWORD_INSTALL,
                LocalDate.of(2026, 6, 19),
                LocalDate.of(2026, 6, 21),
                8,
                List.of("chat app"),
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_EXECUTION);
        assertThat(order.getConfirmedByAdminId()).isEqualTo(99L);
        assertThat(order.getConfirmedAt()).isEqualTo(LocalDateTime.of(2026, 6, 20, 8, 0));
        assertThat(order.getDeductedTransactionId()).isNotNull();
    }

    @Test
    void adminCreatedOrderWithInsufficientBalanceStartsPendingPayment() {
        walletService.balance = BigDecimal.ZERO;

        AsoOrder order = orderService.createAdminOrderForCustomer(10L, 99L, new CreateOrderCommand(
                1L,
                OrderType.KEYWORD_INSTALL,
                LocalDate.of(2026, 6, 19),
                LocalDate.of(2026, 6, 21),
                8,
                List.of("chat app"),
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(order.getConfirmedByAdminId()).isEqualTo(99L);
        assertThat(order.getConfirmedAt()).isNull();
        assertThat(order.getDeductedTransactionId()).isNull();
    }

    @Test
    void createKeywordInstallOrderChargesPerKeywordQuantity() {
        walletService.balance = new BigDecimal("100.00");

        AsoOrder order = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                OrderType.KEYWORD_INSTALL,
                LocalDate.of(2026, 6, 19),
                LocalDate.of(2026, 6, 21),
                8,
                null,
                List.of(
                        new CreateOrderCommand.KeywordQuantity("chat app", 3),
                        new CreateOrderCommand.KeywordQuantity("ai assistant", 2)
                ),
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(order.getQuantity()).isEqualTo(5);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("6.00");
        assertThat(order.getItems())
                .extracting(OrderItem::getItemName, OrderItem::getQuantity, OrderItem::getAmount)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("chat app", 3, new BigDecimal("3.60")),
                        org.assertj.core.groups.Tuple.tuple("ai assistant", 2, new BigDecimal("2.40"))
                );
    }

    @Test
    void createOrderUsesSelectedModulePriceAndSnapshotsModule() {
        walletService.balance = new BigDecimal("100.00");

        AsoOrder order = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L, "US", OrderType.KEYWORD_INSTALL,
                LocalDate.of(2026, 6, 19), LocalDate.of(2026, 6, 19), 8,
                null, List.of(new CreateOrderCommand.KeywordQuantity("chat app", 2, "US")),
                null, null, null, null, null, null, null, 99L
        ));

        assertThat(order.getOrderModuleId()).isEqualTo(99L);
        assertThat(order.getOrderModuleName()).isEqualTo("关键词安装（高级）");
        assertThat(order.getUnitPrice()).isEqualByComparingTo("10.00");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("20.00");
        assertThat(order.getItems()).extracting(OrderItem::getUnitPrice)
                .containsExactly(new BigDecimal("10.00"));
    }

    @Test
    void createKeywordInstallOrderKeepsRegionPerKeywordItemInOneOrder() throws Exception {
        walletService.balance = new BigDecimal("100.00");

        AsoOrder order = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                OrderType.KEYWORD_INSTALL,
                LocalDate.of(2026, 6, 19),
                LocalDate.of(2026, 6, 21),
                8,
                null,
                List.of(
                        new CreateOrderCommand.KeywordQuantity("chat app", 3, "US"),
                        new CreateOrderCommand.KeywordQuantity("chat app", 2, "JP"),
                        new CreateOrderCommand.KeywordQuantity("ai assistant", 4, "JP")
                ),
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(orderRepository.saved).hasSize(1);
        assertThat(order.getRegionCode()).isEqualTo("MULTI");
        assertThat(order.getQuantity()).isEqualTo(9);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("10.80");
        assertThat(order.getItems())
                .extracting(
                        OrderItem::getItemName,
                        OrderItem::getQuantity,
                        OrderItem::getRegionCode,
                        OrderItem::getAmount
                )
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("chat app", 3, "US", new BigDecimal("3.60")),
                        org.assertj.core.groups.Tuple.tuple("chat app", 2, "JP", new BigDecimal("2.40")),
                        org.assertj.core.groups.Tuple.tuple("ai assistant", 4, "JP", new BigDecimal("4.80"))
                );
    }

    @Test
    void createKeywordInstallOrderAllowsEnabledStoreRegionNotLinkedToApp() {
        walletService.balance = new BigDecimal("100.00");
        appRepository.linkedRegions = List.of("US");

        AsoOrder order = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                OrderType.KEYWORD_INSTALL,
                LocalDate.of(2026, 6, 19),
                LocalDate.of(2026, 6, 21),
                8,
                null,
                List.of(new CreateOrderCommand.KeywordQuantity("chat app", 2, "JP")),
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(order.getRegionCode()).isEqualTo("JP");
        assertThat(order.getItems())
                .extracting(OrderItem::getRegionCode, OrderItem::getItemName, OrderItem::getQuantity)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("JP", "chat app", 2));
    }

    @Test
    void createDownloadOrderChargesInclusiveDateRangeTimesDailyQuantity() {
        walletService.balance = new BigDecimal("100.00");

        AsoOrder order = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                OrderType.DOWNLOAD,
                LocalDate.of(2026, 6, 19),
                LocalDate.of(2026, 6, 21),
                null,
                List.of(),
                20,
                null,
                null,
                null,
                null
        ));

        assertThat(order.getTotalDays()).isEqualTo(3);
        assertThat(order.getQuantity()).isEqualTo(60);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("30.00");
        assertThat(order.getExpectedCompletedAt()).isEqualTo(LocalDate.of(2026, 6, 22).atStartOfDay());
    }

    @Test
    void createDownloadOrderKeepsMultipleRegionItemsInOneOrder() {
        walletService.balance = new BigDecimal("100.00");

        AsoOrder order = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                null,
                OrderType.DOWNLOAD,
                LocalDate.of(2026, 6, 19),
                LocalDate.of(2026, 6, 20),
                null,
                List.of(),
                null,
                List.of(
                        new CreateOrderCommand.RegionOrderItem("US", 10, null, null, null, null),
                        new CreateOrderCommand.RegionOrderItem("JP", 5, null, null, null, null)
                ),
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(order.getRegionCode()).isEqualTo("MULTI");
        assertThat(order.getQuantity()).isEqualTo(30);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("15.00");
        assertThat(order.getItems())
                .extracting(OrderItem::getItemType, OrderItem::getRegionCode, OrderItem::getQuantity, OrderItem::getAmount)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("DOWNLOAD", "US", 20, new BigDecimal("10.00")),
                        org.assertj.core.groups.Tuple.tuple("DOWNLOAD", "JP", 10, new BigDecimal("5.00"))
                );
    }

    @Test
    void createDownloadOrderUsesChinaSpecificPrice() {
        walletService.balance = new BigDecimal("100.00");
        pricingRepository.put(PriceCode.DOWNLOAD, "0.50", "0.80");

        AsoOrder order = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                "CN",
                OrderType.DOWNLOAD,
                LocalDate.of(2026, 6, 19),
                LocalDate.of(2026, 6, 20),
                null,
                List.of(),
                null,
                List.of(new CreateOrderCommand.RegionOrderItem("CN", 10, null, null, null, null)),
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(order.getTotalAmount()).isEqualByComparingTo("16.00");
        assertThat(order.getItems()).singleElement()
                .extracting(OrderItem::getAmount)
                .isEqualTo(new BigDecimal("16.00"));
    }

    @Test
    void createDownloadOrderRejectsChinaMixedWithOtherRegions() {
        walletService.balance = new BigDecimal("100.00");

        assertThatThrownBy(() -> orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                null,
                OrderType.DOWNLOAD,
                LocalDate.of(2026, 6, 19),
                LocalDate.of(2026, 6, 20),
                null,
                List.of(),
                null,
                List.of(
                        new CreateOrderCommand.RegionOrderItem("CN", 10, null, null, null, null),
                        new CreateOrderCommand.RegionOrderItem("US", 10, null, null, null, null)
                ),
                null,
                null,
                null,
                null,
                null
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }

    @Test
    void createRatingOrderUsesSeparateFourAndFiveStarPrices() {
        walletService.balance = new BigDecimal("100.00");

        AsoOrder order = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                OrderType.RATING,
                LocalDate.of(2026, 6, 20),
                LocalDate.of(2026, 6, 20),
                null,
                List.of(),
                null,
                2,
                3,
                null,
                null
        ));

        assertThat(order.getQuantity()).isEqualTo(5);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("8.50");
        assertThat(order.getItems())
                .extracting(OrderItem::getItemType, OrderItem::getQuantity, OrderItem::getUnitPrice, OrderItem::getAmount)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("RATING_5", 2, new BigDecimal("2.00"), new BigDecimal("4.00")),
                        org.assertj.core.groups.Tuple.tuple("RATING_4", 3, new BigDecimal("1.50"), new BigDecimal("4.50"))
                );
    }

    @Test
    void createRatingOrderMultipliesDailyCountsByOrderDays() {
        walletService.balance = new BigDecimal("100.00");

        AsoOrder order = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                OrderType.RATING,
                LocalDate.of(2026, 6, 20),
                LocalDate.of(2026, 6, 22),
                null,
                List.of(),
                null,
                2,
                3,
                null,
                null
        ));

        assertThat(order.getTotalDays()).isEqualTo(3);
        assertThat(order.getQuantity()).isEqualTo(15);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("25.50");
        assertThat(order.getItems())
                .extracting(OrderItem::getItemType, OrderItem::getQuantity, OrderItem::getAmount)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("RATING_5", 6, new BigDecimal("12.00")),
                        org.assertj.core.groups.Tuple.tuple("RATING_4", 9, new BigDecimal("13.50"))
                );
    }

    @Test
    void createRatingOrderKeepsMultipleRegionItemsInOneOrder() {
        walletService.balance = new BigDecimal("100.00");

        AsoOrder order = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                null,
                OrderType.RATING,
                LocalDate.of(2026, 6, 20),
                LocalDate.of(2026, 6, 20),
                null,
                List.of(),
                null,
                List.of(
                        new CreateOrderCommand.RegionOrderItem("US", null, 2, 1, null, null),
                        new CreateOrderCommand.RegionOrderItem("JP", null, 0, 3, null, null)
                ),
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(order.getRegionCode()).isEqualTo("MULTI");
        assertThat(order.getQuantity()).isEqualTo(6);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("10.00");
        assertThat(order.getItems())
                .extracting(OrderItem::getItemType, OrderItem::getRegionCode, OrderItem::getQuantity, OrderItem::getAmount)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("RATING_5", "US", 2, new BigDecimal("4.00")),
                        org.assertj.core.groups.Tuple.tuple("RATING_4", "US", 1, new BigDecimal("1.50")),
                        org.assertj.core.groups.Tuple.tuple("RATING_4", "JP", 3, new BigDecimal("4.50"))
                );
    }

    @Test
    void createRatingOrderRejectsMainlandChinaRegion() {
        walletService.balance = new BigDecimal("100.00");

        assertThatThrownBy(() -> orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                null,
                OrderType.RATING,
                LocalDate.of(2026, 6, 20),
                LocalDate.of(2026, 6, 20),
                null,
                List.of(),
                null,
                List.of(new CreateOrderCommand.RegionOrderItem("CN", null, 1, 0, null, null)),
                null,
                null,
                null,
                null,
                null
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STORE_REGION_NOT_SUPPORTED);
    }

    @Test
    void createRatingOrderMultipliesRegionalDailyCountsByOrderDays() {
        walletService.balance = new BigDecimal("100.00");

        AsoOrder order = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                null,
                OrderType.RATING,
                LocalDate.of(2026, 6, 20),
                LocalDate.of(2026, 6, 21),
                null,
                List.of(),
                null,
                List.of(
                        new CreateOrderCommand.RegionOrderItem("US", null, 2, 1, null, null),
                        new CreateOrderCommand.RegionOrderItem("JP", null, 0, 3, null, null)
                ),
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(order.getTotalDays()).isEqualTo(2);
        assertThat(order.getQuantity()).isEqualTo(12);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("20.00");
        assertThat(order.getItems())
                .extracting(OrderItem::getItemType, OrderItem::getRegionCode, OrderItem::getQuantity, OrderItem::getAmount)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("RATING_5", "US", 4, new BigDecimal("8.00")),
                        org.assertj.core.groups.Tuple.tuple("RATING_4", "US", 2, new BigDecimal("3.00")),
                        org.assertj.core.groups.Tuple.tuple("RATING_4", "JP", 6, new BigDecimal("9.00"))
                );
    }

    @Test
    void createReviewOrderKeepsMultipleRegionItemsInOneOrder() {
        walletService.balance = new BigDecimal("100.00");

        AsoOrder order = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                null,
                OrderType.REVIEW,
                LocalDate.of(2026, 6, 20),
                LocalDate.of(2026, 6, 20),
                null,
                List.of(),
                null,
                List.of(
                        new CreateOrderCommand.RegionOrderItem("US", null, null, null, 1, 2),
                        new CreateOrderCommand.RegionOrderItem("JP", null, null, null, 3, 0)
                ),
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(order.getRegionCode()).isEqualTo("MULTI");
        assertThat(order.getQuantity()).isEqualTo(6);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("17.00");
        assertThat(order.getItems())
                .extracting(OrderItem::getItemType, OrderItem::getRegionCode, OrderItem::getQuantity, OrderItem::getAmount)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("REVIEW_5", "US", 1, new BigDecimal("3.00")),
                        org.assertj.core.groups.Tuple.tuple("REVIEW_4", "US", 2, new BigDecimal("5.00")),
                        org.assertj.core.groups.Tuple.tuple("REVIEW_5", "JP", 3, new BigDecimal("9.00"))
                );
    }

    @Test
    void createReviewOrderStoresStructuredCommentDetails() {
        walletService.balance = new BigDecimal("100.00");

        AsoOrder order = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                null,
                OrderType.REVIEW,
                LocalDate.of(2026, 6, 20),
                LocalDate.of(2026, 6, 20),
                null,
                null,
                null,
                null,
                List.of(),
                null,
                List.of(),
                List.of(
                        new CreateOrderCommand.ReviewDetail("US", 5, "Great app", "Very useful every day"),
                        new CreateOrderCommand.ReviewDetail("JP", 4, "Solid", "Stable and easy to use")
                ),
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(order.getQuantity()).isEqualTo(2);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("5.50");
        assertThat(order.getCommentDetails())
                .extracting(
                        OrderCommentDetail::getRegionCode,
                        OrderCommentDetail::getStarLevel,
                        OrderCommentDetail::getCommentTitle,
                        OrderCommentDetail::getCommentContent
                )
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("US", 5, "Great app", "Very useful every day"),
                        org.assertj.core.groups.Tuple.tuple("JP", 4, "Solid", "Stable and easy to use")
                );
        assertThat(orderCommentDetailRepository.detailsByOrderId.get(order.getId())).hasSize(2);
    }

    @Test
    void createReviewOrderRejectsMainlandChinaRegion() {
        walletService.balance = new BigDecimal("100.00");

        assertThatThrownBy(() -> orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                null,
                OrderType.REVIEW,
                LocalDate.of(2026, 6, 20),
                LocalDate.of(2026, 6, 20),
                null,
                null,
                null,
                null,
                List.of(),
                null,
                List.of(),
                List.of(new CreateOrderCommand.ReviewDetail("CN", 5, "Great app", "Very useful every day")),
                null,
                null,
                null,
                null,
                null
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STORE_REGION_NOT_SUPPORTED);
    }

    @Test
    void createKeywordInstallOrderAcceptsSixteenExecutionHours() {
        walletService.balance = new BigDecimal("100.00");

        AsoOrder order = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                OrderType.KEYWORD_INSTALL,
                LocalDate.of(2026, 6, 20),
                LocalDate.of(2026, 6, 20),
                16,
                List.of("chat app"),
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(order.getExecutionHours()).isEqualTo(16);
    }
    @Test
    void createOrderRejectsUnsupportedExecutionHours() {
        assertThatThrownBy(() -> orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                OrderType.KEYWORD_INSTALL,
                LocalDate.of(2026, 6, 20),
                LocalDate.of(2026, 6, 20),
                9,
                List.of("chat app"),
                null,
                null,
                null,
                null,
                null
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXECUTION_HOURS_INVALID);
    }

    @Test
    void createOrderWithInsufficientBalanceSavesPendingPaymentOrder() {
        walletService.balance = new BigDecimal("1.00");

        AsoOrder order = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                OrderType.DOWNLOAD,
                LocalDate.of(2026, 6, 19),
                LocalDate.of(2026, 6, 21),
                null,
                List.of(),
                20,
                null,
                null,
                null,
                null
        ));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("30.00");
        assertThat(order.getDeductedTransactionId()).isNull();
        assertThat(order.getBalanceBefore()).isEqualByComparingTo("1.00");
        assertThat(order.getBalanceAfter()).isEqualByComparingTo("1.00");
        assertThat(orderRepository.saved).hasSize(1);
        assertThat(orderItemRepository.itemsByOrderId.get(order.getId())).hasSize(1);
        assertThat(walletService.balance).isEqualByComparingTo("1.00");
    }

    @Test
    void resubmitPendingPaymentOrderKeepsPendingPaymentWhenBalanceStillInsufficient() {
        walletService.balance = new BigDecimal("1.00");
        AsoOrder pending = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                OrderType.DOWNLOAD,
                LocalDate.of(2026, 6, 19),
                LocalDate.of(2026, 6, 21),
                null,
                List.of(),
                20,
                null,
                null,
                null,
                null
        ));

        AsoOrder resubmitted = orderService.resubmitPendingPaymentOrder(10L, pending.getId(), new CreateOrderCommand(
                1L,
                OrderType.DOWNLOAD,
                LocalDate.of(2026, 6, 19),
                LocalDate.of(2026, 6, 20),
                null,
                List.of(),
                10,
                null,
                null,
                null,
                null
        ));

        assertThat(resubmitted.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(resubmitted.getTotalAmount()).isEqualByComparingTo("10.00");
        assertThat(resubmitted.getDeductedTransactionId()).isNull();
        assertThat(resubmitted.getBalanceBefore()).isEqualByComparingTo("1.00");
        assertThat(resubmitted.getBalanceAfter()).isEqualByComparingTo("1.00");
        assertThat(orderItemRepository.itemsByOrderId.get(pending.getId()))
                .extracting(OrderItem::getQuantity, OrderItem::getAmount)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(20, new BigDecimal("10.00")));
        assertThat(walletService.balance).isEqualByComparingTo("1.00");
    }

    @Test
    void resubmitPendingPaymentOrderChargesAndMovesToPendingConfirmWhenBalanceIsEnough() {
        walletService.balance = new BigDecimal("1.00");
        AsoOrder pending = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                OrderType.DOWNLOAD,
                LocalDate.of(2026, 6, 19),
                LocalDate.of(2026, 6, 21),
                null,
                List.of(),
                20,
                null,
                null,
                null,
                null
        ));
        walletService.balance = new BigDecimal("100.00");

        AsoOrder resubmitted = orderService.resubmitPendingPaymentOrder(10L, pending.getId(), new CreateOrderCommand(
                1L,
                OrderType.RATING,
                LocalDate.of(2026, 6, 20),
                LocalDate.of(2026, 6, 20),
                null,
                List.of(),
                null,
                2,
                1,
                null,
                null
        ));

        assertThat(resubmitted.getStatus()).isEqualTo(OrderStatus.PENDING_CONFIRM);
        assertThat(resubmitted.getOrderType()).isEqualTo(OrderType.RATING);
        assertThat(resubmitted.getTotalAmount()).isEqualByComparingTo("5.50");
        assertThat(resubmitted.getBalanceBefore()).isEqualByComparingTo("100.00");
        assertThat(resubmitted.getBalanceAfter()).isEqualByComparingTo("94.50");
        assertThat(resubmitted.getDeductedTransactionId()).isEqualTo(1L);
        assertThat(orderItemRepository.itemsByOrderId.get(pending.getId()))
                .extracting(OrderItem::getItemType, OrderItem::getQuantity, OrderItem::getAmount)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("RATING_5", 2, new BigDecimal("4.00")),
                        org.assertj.core.groups.Tuple.tuple("RATING_4", 1, new BigDecimal("1.50"))
                );
        assertThat(walletService.balance).isEqualByComparingTo("94.50");
    }

    @Test
    void editPendingConfirmOrderChargesOnlyTheIncrease() {
        walletService.balance = new BigDecimal("100.00");
        AsoOrder order = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L, OrderType.DOWNLOAD, LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 20),
                null, List.of(), 10, null, null, null, null));
        assertThat(walletService.balance).isEqualByComparingTo("95.00");

        AsoOrder updated = orderService.resubmitEditableOrder(10L, order.getId(), new CreateOrderCommand(
                1L, OrderType.DOWNLOAD, LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 20),
                null, List.of(), 14, null, null, null, null));

        assertThat(updated.getStatus()).isEqualTo(OrderStatus.PENDING_CONFIRM);
        assertThat(updated.getTotalAmount()).isEqualByComparingTo("7.00");
        assertThat(walletService.balance).isEqualByComparingTo("93.00");
    }

    @Test
    void editPendingConfirmOrderRefundsOnlyTheDecrease() {
        walletService.balance = new BigDecimal("100.00");
        AsoOrder order = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L, OrderType.DOWNLOAD, LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 20),
                null, List.of(), 10, null, null, null, null));

        AsoOrder updated = orderService.resubmitEditableOrder(10L, order.getId(), new CreateOrderCommand(
                1L, OrderType.DOWNLOAD, LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 20),
                null, List.of(), 6, null, null, null, null));

        assertThat(updated.getTotalAmount()).isEqualByComparingTo("3.00");
        assertThat(walletService.balance).isEqualByComparingTo("97.00");
    }

    @Test
    void editOrderIsRejectedAfterAdminConfirmation() {
        AsoOrder order = sampleOrder();
        order.setId(801L);
        order.setStatus(OrderStatus.PENDING_EXECUTION);
        orderRepository.orders.put(order.getId(), order);

        assertThatThrownBy(() -> orderService.resubmitEditableOrder(10L, order.getId(), new CreateOrderCommand(
                1L, OrderType.DOWNLOAD, LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 20),
                null, List.of(), 6, null, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_STATUS_INVALID);
    }

    @Test
    void payPendingPaymentOrderUsesExistingDetailsWithoutEditing() {
        walletService.balance = new BigDecimal("1.00");
        AsoOrder pending = orderService.createCustomerOrder(10L, new CreateOrderCommand(
                1L,
                OrderType.DOWNLOAD,
                LocalDate.of(2026, 6, 19),
                LocalDate.of(2026, 6, 20),
                null,
                List.of(),
                10,
                null,
                null,
                null,
                null
        ));
        walletService.balance = new BigDecimal("100.00");

        AsoOrder paid = orderService.payPendingPaymentOrder(10L, pending.getId());

        assertThat(paid.getStatus()).isEqualTo(OrderStatus.PENDING_CONFIRM);
        assertThat(paid.getTotalAmount()).isEqualByComparingTo("10.00");
        assertThat(paid.getBalanceAfter()).isEqualByComparingTo("90.00");
        assertThat(paid.getDeductedTransactionId()).isNotNull();
    }

    @Test
    void payAdminCreatedPendingPaymentOrderSkipsConfirmation() {
        walletService.balance = BigDecimal.ZERO;
        AsoOrder pending = orderService.createAdminOrderForCustomer(10L, 99L, new CreateOrderCommand(
                1L,
                OrderType.DOWNLOAD,
                LocalDate.of(2026, 6, 19),
                LocalDate.of(2026, 6, 20),
                null,
                List.of(),
                10,
                null,
                null,
                null,
                null
        ));
        walletService.balance = new BigDecimal("100.00");

        AsoOrder paid = orderService.payPendingPaymentOrder(10L, pending.getId());

        assertThat(paid.getStatus()).isEqualTo(OrderStatus.PENDING_EXECUTION);
        assertThat(paid.getConfirmedByAdminId()).isEqualTo(99L);
        assertThat(paid.getConfirmedAt()).isEqualTo(LocalDateTime.of(2026, 6, 20, 8, 0));
    }

    @Test
    void executeOrderCompletesImmediatelyWhenExpectedCompletionHasPassed() {
        AsoOrder existing = sampleOrder();
        existing.setId(99L);
        existing.setStatus(OrderStatus.PENDING_EXECUTION);
        existing.setExpectedCompletedAt(LocalDate.of(2026, 6, 19).atStartOfDay());
        orderRepository.orders.put(99L, existing);

        AsoOrder executed = orderService.executeOrder(99L, 7L);

        assertThat(executed.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(executed.getExecutedByAdminId()).isEqualTo(7L);
        assertThat(executed.getCompletedAt()).isEqualTo(expectedNow());
    }

    @Test
    void executeKeywordInstallOrderCompletesByExecutionHoursAfterStart() {
        AsoOrder existing = sampleOrder();
        existing.setId(98L);
        existing.setOrderType(OrderType.KEYWORD_INSTALL);
        existing.setExecutionHours(1);
        existing.setStatus(OrderStatus.PENDING_EXECUTION);
        existing.setExpectedCompletedAt(LocalDate.of(2026, 6, 19).atStartOfDay());
        orderRepository.orders.put(98L, existing);

        AsoOrder executed = orderService.executeOrder(98L, 7L);

        assertThat(executed.getStatus()).isEqualTo(OrderStatus.EXECUTING);
        assertThat(executed.getExecutedAt()).isEqualTo(expectedNow());
        assertThat(executed.getExpectedCompletedAt()).isEqualTo(expectedNow().plusHours(1));
        assertThat(executed.getCompletedAt()).isNull();
    }

    @Test
    void executeOrdersExecutesAllPendingExecutionOrders() {
        AsoOrder first = sampleOrder();
        first.setId(200L);
        first.setStatus(OrderStatus.PENDING_EXECUTION);
        first.setExpectedCompletedAt(LocalDate.of(2026, 6, 21).atStartOfDay());
        orderRepository.orders.put(200L, first);

        AsoOrder second = sampleOrder();
        second.setId(201L);
        second.setStatus(OrderStatus.PENDING_EXECUTION);
        second.setExpectedCompletedAt(LocalDate.of(2026, 6, 19).atStartOfDay());
        orderRepository.orders.put(201L, second);

        List<AsoOrder> result = orderService.executeOrders(List.of(200L, 201L), 7L);

        assertThat(result).extracting(AsoOrder::getId).containsExactly(200L, 201L);
        assertThat(orderRepository.orders.get(200L).getStatus()).isEqualTo(OrderStatus.EXECUTING);
        assertThat(orderRepository.orders.get(201L).getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(result).allSatisfy(order -> assertThat(order.getExecutedByAdminId()).isEqualTo(7L));
    }

    @Test
    void executeOrdersRejectsNonPendingExecutionOrder() {
        AsoOrder first = sampleOrder();
        first.setId(202L);
        first.setStatus(OrderStatus.PENDING_EXECUTION);
        orderRepository.orders.put(202L, first);

        AsoOrder invalid = sampleOrder();
        invalid.setId(203L);
        invalid.setStatus(OrderStatus.PENDING_CONFIRM);
        orderRepository.orders.put(203L, invalid);

        assertThatThrownBy(() -> orderService.executeOrders(List.of(202L, 203L), 7L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_STATUS_INVALID);
        assertThat(orderRepository.orders.get(202L).getStatus()).isEqualTo(OrderStatus.PENDING_EXECUTION);
    }

    @Test
    void pauseOrderMovesExecutingOrderToPaused() {
        AsoOrder existing = sampleOrder();
        existing.setId(204L);
        existing.setStatus(OrderStatus.EXECUTING);
        existing.setExecutedAt(expectedNow().minusHours(1));
        existing.setExpectedCompletedAt(expectedNow().plusDays(1));
        orderRepository.orders.put(204L, existing);
        OrderItem item = new OrderItem();
        item.setId(2040L);
        item.setQuantity(60);
        item.setUnitPrice(new BigDecimal("0.50"));
        orderItemRepository.itemsByOrderId.put(204L, new ArrayList<>(List.of(item)));

        AsoOrder paused = orderService.pauseOrder(204L, 7L);

        assertThat(paused.getStatus()).isEqualTo(OrderStatus.PAUSED);
        assertThat(paused.getExecutedAt()).isEqualTo(expectedNow().minusHours(1));
        assertThat(paused.getExpectedCompletedAt()).isEqualTo(expectedNow().plusDays(1));
    }

    @Test
    void pauseOrderRejectsNonExecutingOrder() {
        AsoOrder existing = sampleOrder();
        existing.setId(205L);
        existing.setStatus(OrderStatus.PENDING_EXECUTION);
        orderRepository.orders.put(205L, existing);

        assertThatThrownBy(() -> orderService.pauseOrder(205L, 7L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_STATUS_INVALID);
        assertThat(orderRepository.orders.get(205L).getStatus()).isEqualTo(OrderStatus.PENDING_EXECUTION);
    }

    @Test
    void batchPauseMovesExecutingOrdersToPaused() {
        AsoOrder first = sampleOrder();
        first.setId(206L);
        first.setStatus(OrderStatus.EXECUTING);
        AsoOrder second = sampleOrder();
        second.setId(207L);
        second.setStatus(OrderStatus.EXECUTING);
        orderRepository.orders.put(206L, first);
        orderRepository.orders.put(207L, second);

        List<AsoOrder> paused = orderService.pauseOrders(List.of(206L, 207L), 7L);

        assertThat(paused).extracting(AsoOrder::getStatus)
                .containsExactly(OrderStatus.PAUSED, OrderStatus.PAUSED);
    }
    @Test
    void updatePausedOrderRefundsImmediatelyWhenQuantityDecreases() {
        AsoOrder paused = sampleOrder();
        paused.setId(208L);
        paused.setStatus(OrderStatus.PAUSED);
        orderRepository.orders.put(208L, paused);
        OrderItem item = new OrderItem();
        item.setId(2080L);
        item.setQuantity(60);
        item.setUnitPrice(new BigDecimal("0.50"));
        item.setAmount(new BigDecimal("30.00"));
        orderItemRepository.itemsByOrderId.put(208L, new ArrayList<>(List.of(item)));
        walletService.balance = new BigDecimal("50.00");

        AsoOrder updated = orderService.updatePausedOrder(
                208L,
                7L,
                List.of(new OrderService.PausedItemEdit(2080L, 40, 10))
        );

        assertThat(updated.getQuantity()).isEqualTo(40);
        assertThat(updated.getTotalAmount()).isEqualByComparingTo("20.00");
        assertThat(updated.getItems()).singleElement()
                .extracting(OrderItem::getQuantity, OrderItem::getCompletedQuantity, OrderItem::getAmount)
                .containsExactly(40, 10, new BigDecimal("20.00"));
        assertThat(walletService.balance).isEqualByComparingTo("60.00");
    }

    @Test
    void updatePausedOrderDebitsImmediatelyWhenQuantityIncreases() {
        AsoOrder paused = sampleOrder();
        paused.setId(209L);
        paused.setStatus(OrderStatus.PAUSED);
        orderRepository.orders.put(209L, paused);
        OrderItem item = new OrderItem();
        item.setId(2090L);
        item.setQuantity(60);
        item.setUnitPrice(new BigDecimal("0.50"));
        item.setAmount(new BigDecimal("30.00"));
        orderItemRepository.itemsByOrderId.put(209L, new ArrayList<>(List.of(item)));
        walletService.balance = new BigDecimal("50.00");

        AsoOrder updated = orderService.updatePausedOrder(
                209L,
                7L,
                List.of(new OrderService.PausedItemEdit(2090L, 80, 15))
        );

        assertThat(updated.getQuantity()).isEqualTo(80);
        assertThat(updated.getTotalAmount()).isEqualByComparingTo("40.00");
        assertThat(walletService.balance).isEqualByComparingTo("40.00");
    }

    @Test
    void updatePausedOrderRejectsIncreaseWhenBalanceIsInsufficient() {
        AsoOrder paused = sampleOrder();
        paused.setId(2091L);
        paused.setStatus(OrderStatus.PAUSED);
        orderRepository.orders.put(2091L, paused);
        OrderItem item = new OrderItem();
        item.setId(20910L);
        item.setQuantity(60);
        item.setUnitPrice(new BigDecimal("0.50"));
        item.setAmount(new BigDecimal("30.00"));
        orderItemRepository.itemsByOrderId.put(2091L, new ArrayList<>(List.of(item)));
        walletService.balance = new BigDecimal("5.00");

        assertThatThrownBy(() -> orderService.updatePausedOrder(
                2091L,
                7L,
                List.of(new OrderService.PausedItemEdit(20910L, 80, 10))
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BALANCE_NOT_ENOUGH);
        assertThat(walletService.balance).isEqualByComparingTo("5.00");
    }
    @Test
    void resumeOrderMovesPausedOrderToExecuting() {
        AsoOrder existing = sampleOrder();
        existing.setId(210L);
        existing.setStatus(OrderStatus.PAUSED);
        existing.setExecutedAt(expectedNow().minusHours(2));
        existing.setExpectedCompletedAt(expectedNow().plusDays(2));
        orderRepository.orders.put(210L, existing);

        AsoOrder resumed = orderService.resumeOrder(210L, 7L);

        assertThat(resumed.getStatus()).isEqualTo(OrderStatus.EXECUTING);
        assertThat(resumed.getExecutedAt()).isEqualTo(expectedNow().minusHours(2));
        assertThat(resumed.getExpectedCompletedAt()).isEqualTo(expectedNow().plusDays(2));
    }

    @Test
    void resumeOrderRejectsNonPausedOrder() {
        AsoOrder existing = sampleOrder();
        existing.setId(211L);
        existing.setStatus(OrderStatus.EXECUTING);
        orderRepository.orders.put(211L, existing);

        assertThatThrownBy(() -> orderService.resumeOrder(211L, 7L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_STATUS_INVALID);
        assertThat(orderRepository.orders.get(211L).getStatus()).isEqualTo(OrderStatus.EXECUTING);
    }

    @Test
    void confirmOrderMovesPendingConfirmToPendingExecution() {
        AsoOrder existing = sampleOrder();
        existing.setId(100L);
        existing.setStatus(OrderStatus.PENDING_CONFIRM);
        orderRepository.orders.put(100L, existing);

        AsoOrder confirmed = orderService.confirmOrder(100L, 7L);

        assertThat(confirmed.getStatus()).isEqualTo(OrderStatus.PENDING_EXECUTION);
        assertThat(confirmed.getConfirmedByAdminId()).isEqualTo(7L);
        assertThat(confirmed.getConfirmedAt()).isEqualTo(expectedNow());
    }

    @Test
    void confirmOrdersMovesAllPendingConfirmOrdersToPendingExecution() {
        AsoOrder first = sampleOrder();
        first.setId(120L);
        first.setStatus(OrderStatus.PENDING_CONFIRM);
        AsoOrder second = sampleOrder();
        second.setId(121L);
        second.setStatus(OrderStatus.PENDING_CONFIRM);
        orderRepository.orders.put(120L, first);
        orderRepository.orders.put(121L, second);

        List<AsoOrder> confirmed = orderService.confirmOrders(List.of(120L, 121L), 7L);

        assertThat(confirmed).extracting(AsoOrder::getStatus)
                .containsExactly(OrderStatus.PENDING_EXECUTION, OrderStatus.PENDING_EXECUTION);
        assertThat(confirmed).extracting(AsoOrder::getConfirmedByAdminId)
                .containsExactly(7L, 7L);
        assertThat(confirmed).extracting(AsoOrder::getConfirmedAt)
                .containsExactly(expectedNow(), expectedNow());
    }

    @Test
    void confirmOrdersRejectsNonPendingConfirmOrder() {
        AsoOrder pending = sampleOrder();
        pending.setId(122L);
        pending.setStatus(OrderStatus.PENDING_CONFIRM);
        AsoOrder invalid = sampleOrder();
        invalid.setId(123L);
        invalid.setStatus(OrderStatus.PENDING_EXECUTION);
        orderRepository.orders.put(122L, pending);
        orderRepository.orders.put(123L, invalid);

        assertThatThrownBy(() -> orderService.confirmOrders(List.of(122L, 123L), 7L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_STATUS_INVALID);
        assertThat(orderRepository.orders.get(122L).getStatus()).isEqualTo(OrderStatus.PENDING_CONFIRM);
        assertThat(orderRepository.orders.get(123L).getStatus()).isEqualTo(OrderStatus.PENDING_EXECUTION);
    }

    @Test
    void cancelOrderRefundsDeductedPendingOrderAndMarksCancelled() {
        AsoOrder existing = sampleOrder();
        existing.setId(110L);
        existing.setStatus(OrderStatus.PENDING_EXECUTION);
        existing.setDeductedTransactionId(15L);
        existing.setTotalAmount(new BigDecimal("30.00"));
        orderRepository.orders.put(110L, existing);
        walletService.balance = new BigDecimal("70.00");

        AsoOrder cancelled = orderService.cancelOrder(110L, 7L, "Customer requested cancellation");

        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(cancelled.getRejectReason()).isEqualTo("Customer requested cancellation");
        assertThat(cancelled.getRefundTransactionId()).isEqualTo(1L);
        assertThat(walletService.balance).isEqualByComparingTo("100.00");
    }


    @Test
    void cancelOrderRejectsExecutingOrderWithoutRefunding() {
        AsoOrder existing = sampleOrder();
        existing.setId(111L);
        existing.setStatus(OrderStatus.EXECUTING);
        existing.setDeductedTransactionId(16L);
        orderRepository.orders.put(111L, existing);
        walletService.balance = new BigDecimal("70.00");

        assertThatThrownBy(() -> orderService.cancelOrder(111L, 7L, "Too late"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_STATUS_INVALID);

        assertThat(orderRepository.orders.get(111L).getStatus()).isEqualTo(OrderStatus.EXECUTING);
        assertThat(orderRepository.orders.get(111L).getRefundTransactionId()).isNull();
        assertThat(walletService.balance).isEqualByComparingTo("70.00");
    }

    @Test
    void listCustomerOrdersReturnsOnlyCurrentCustomerAndAppliesFilters() {
        AsoOrder matching = sampleOrder();
        matching.setId(101L);
        matching.setCustomerId(10L);
        matching.setStoreType(StoreType.APP_STORE);
        matching.setStatus(OrderStatus.PENDING_CONFIRM);
        orderRepository.orders.put(101L, matching);
        OrderItem matchingItem = new OrderItem();
        matchingItem.setItemType("DOWNLOAD");
        matchingItem.setQuantity(60);
        matchingItem.setUnitPrice(new BigDecimal("0.50"));
        matchingItem.setAmount(new BigDecimal("30.00"));
        orderItemRepository.saveAll(101L, List.of(matchingItem));

        AsoOrder otherCustomer = sampleOrder();
        otherCustomer.setId(102L);
        otherCustomer.setCustomerId(11L);
        otherCustomer.setStoreType(StoreType.APP_STORE);
        otherCustomer.setStatus(OrderStatus.PENDING_CONFIRM);
        orderRepository.orders.put(102L, otherCustomer);

        AsoOrder otherStore = sampleOrder();
        otherStore.setId(103L);
        otherStore.setCustomerId(10L);
        otherStore.setStoreType(StoreType.GOOGLE_PLAY);
        otherStore.setStatus(OrderStatus.PENDING_CONFIRM);
        orderRepository.orders.put(103L, otherStore);

        List<AsoOrder> result = orderService.listCustomerOrders(10L, new OrderQuery(
                StoreType.APP_STORE,
                OrderStatus.PENDING_CONFIRM
        ));

        assertThat(result).extracting(AsoOrder::getId).containsExactly(101L);
        assertThat(result.get(0).getItems()).hasSize(1);
        assertThat(result.get(0).getItems().get(0).getItemType()).isEqualTo("DOWNLOAD");
    }

    @Test
    void listAdminOrdersAppliesStoreAndStatusFilters() {
        AsoOrder pendingApple = sampleOrder();
        pendingApple.setId(104L);
        pendingApple.setStoreType(StoreType.APP_STORE);
        pendingApple.setStatus(OrderStatus.PENDING_CONFIRM);
        orderRepository.orders.put(104L, pendingApple);

        AsoOrder executingApple = sampleOrder();
        executingApple.setId(105L);
        executingApple.setStoreType(StoreType.APP_STORE);
        executingApple.setStatus(OrderStatus.EXECUTING);
        orderRepository.orders.put(105L, executingApple);

        List<AsoOrder> result = orderService.listAdminOrders(new OrderQuery(
                StoreType.APP_STORE,
                OrderStatus.PENDING_CONFIRM
        ));

        assertThat(result).extracting(AsoOrder::getId).containsExactly(104L);
    }

    @Test
    void getCustomerOrderReturnsOwnedOrderWithItems() {
        AsoOrder order = sampleOrder();
        order.setId(114L);
        order.setCustomerId(10L);
        orderRepository.orders.put(114L, order);
        OrderItem item = new OrderItem();
        item.setItemType("KEYWORD_INSTALL");
        item.setItemName("chat app");
        item.setRegionCode("US");
        item.setQuantity(3);
        orderItemRepository.saveAll(114L, List.of(item));

        AsoOrder result = orderService.getCustomerOrder(10L, 114L);

        assertThat(result.getId()).isEqualTo(114L);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getItemName()).isEqualTo("chat app");
    }

    @Test
    void getCustomerOrderRejectsOtherCustomerOrder() {
        AsoOrder order = sampleOrder();
        order.setId(115L);
        order.setCustomerId(11L);
        orderRepository.orders.put(115L, order);

        assertThatThrownBy(() -> orderService.getCustomerOrder(10L, 115L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    void getAdminOrderReturnsAnyOrderWithItems() {
        AsoOrder order = sampleOrder();
        order.setId(116L);
        order.setCustomerId(11L);
        orderRepository.orders.put(116L, order);
        CustomerAccount customer = new CustomerAccount();
        customer.setId(11L);
        customer.setUsername("test_user");
        customer.setEmail("test@example.com");
        customerAccountRepository.customers.put(11L, customer);
        OrderItem item = new OrderItem();
        item.setItemType("DOWNLOAD");
        item.setQuantity(20);
        orderItemRepository.saveAll(116L, List.of(item));

        AsoOrder result = orderService.getAdminOrder(116L);

        assertThat(result.getCustomerId()).isEqualTo(11L);
        assertThat(result.getCustomerUsername()).isEqualTo("test_user");
        assertThat(result.getCustomerEmail()).isEqualTo("test@example.com");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getItemType()).isEqualTo("DOWNLOAD");
    }

    @Test
    void listAdminOrdersCompletesExpiredExecutingOrdersBeforeFiltering() {
        AsoOrder expiredExecuting = sampleOrder();
        expiredExecuting.setId(108L);
        expiredExecuting.setStatus(OrderStatus.EXECUTING);
        expiredExecuting.setExpectedCompletedAt(LocalDate.of(2026, 6, 19).atStartOfDay());
        orderRepository.orders.put(108L, expiredExecuting);

        AsoOrder activeExecuting = sampleOrder();
        activeExecuting.setId(109L);
        activeExecuting.setStatus(OrderStatus.EXECUTING);
        activeExecuting.setExpectedCompletedAt(LocalDate.of(2026, 6, 21).atStartOfDay());
        orderRepository.orders.put(109L, activeExecuting);

        List<AsoOrder> result = orderService.listAdminOrders(new OrderQuery(
                null,
                OrderStatus.EXECUTING
        ));

        assertThat(result).extracting(AsoOrder::getId).containsExactly(109L);
        assertThat(orderRepository.orders.get(108L).getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(orderRepository.orders.get(108L).getCompletedAt())
                .isEqualTo(expectedNow());
    }

    @Test
    void completeExpiredExecutingOrdersReturnsCompletedCount() {
        AsoOrder expiredExecuting = sampleOrder();
        expiredExecuting.setId(112L);
        expiredExecuting.setStatus(OrderStatus.EXECUTING);
        expiredExecuting.setExpectedCompletedAt(LocalDate.of(2026, 6, 19).atStartOfDay());
        orderRepository.orders.put(112L, expiredExecuting);

        AsoOrder activeExecuting = sampleOrder();
        activeExecuting.setId(113L);
        activeExecuting.setStatus(OrderStatus.EXECUTING);
        activeExecuting.setExpectedCompletedAt(LocalDate.of(2026, 6, 21).atStartOfDay());
        orderRepository.orders.put(113L, activeExecuting);

        int completedCount = orderService.completeExpiredExecutingOrders();

        assertThat(completedCount).isEqualTo(1);
        assertThat(orderRepository.orders.get(112L).getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(orderRepository.orders.get(112L).getCompletedAt())
                .isEqualTo(expectedNow());
        assertThat(orderRepository.orders.get(113L).getStatus()).isEqualTo(OrderStatus.EXECUTING);
    }

    @Test
    void completeExpiredPausedOrderRefundsEachUnfinishedItem() {
        AsoOrder paused = sampleOrder();
        paused.setId(114L);
        paused.setStatus(OrderStatus.PAUSED);
        paused.setExpectedCompletedAt(LocalDate.of(2026, 6, 19).atStartOfDay());
        paused.setDeductedTransactionId(20L);
        orderRepository.orders.put(114L, paused);

        OrderItem first = new OrderItem();
        first.setId(1141L);
        first.setQuantity(10);
        first.setCompletedQuantity(4);
        first.setUnitPrice(new BigDecimal("2.00"));
        OrderItem second = new OrderItem();
        second.setId(1142L);
        second.setQuantity(5);
        second.setCompletedQuantity(3);
        second.setUnitPrice(new BigDecimal("3.00"));
        orderItemRepository.itemsByOrderId.put(114L, new ArrayList<>(List.of(first, second)));
        walletService.balance = new BigDecimal("50.00");

        int completedCount = orderService.completeExpiredExecutingOrders();

        assertThat(completedCount).isEqualTo(1);
        assertThat(paused.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(paused.getRefundAmount()).isEqualByComparingTo("18.00");
        assertThat(paused.getRefundTransactionId()).isEqualTo(1L);
        assertThat(walletService.balance).isEqualByComparingTo("68.00");
    }

    @Test
    void listAdminOrdersAppliesKeywordRegionTypeAndDateFilters() {
        AsoOrder matching = sampleOrder();
        matching.setId(106L);
        matching.setOrderNo("YO-MATCH");
        matching.setAppIdentifier("bundle.match");
        matching.setRegionCode("US");
        matching.setOrderType(OrderType.DOWNLOAD);
        matching.setOrderStartDate(LocalDate.of(2026, 6, 20));
        matching.setOrderEndDate(LocalDate.of(2026, 6, 22));
        matching.setCreatedAt(LocalDate.of(2026, 6, 20).atStartOfDay());
        orderRepository.orders.put(106L, matching);

        AsoOrder otherRegion = sampleOrder();
        otherRegion.setId(107L);
        otherRegion.setOrderNo("YO-MATCH-2");
        otherRegion.setAppIdentifier("bundle.match");
        otherRegion.setRegionCode("JP");
        otherRegion.setOrderType(OrderType.DOWNLOAD);
        otherRegion.setOrderStartDate(LocalDate.of(2026, 6, 20));
        otherRegion.setOrderEndDate(LocalDate.of(2026, 6, 22));
        otherRegion.setCreatedAt(LocalDate.of(2026, 6, 20).atStartOfDay());
        orderRepository.orders.put(107L, otherRegion);

        List<AsoOrder> result = orderService.listAdminOrders(new OrderQuery(
                StoreType.APP_STORE,
                null,
                "bundle.match",
                null,
                null,
                "US",
                OrderType.DOWNLOAD,
                false,
                LocalDate.of(2026, 6, 19),
                LocalDate.of(2026, 6, 23),
                LocalDate.of(2026, 6, 20),
                LocalDate.of(2026, 6, 20)
        ));

        assertThat(result).extracting(AsoOrder::getId).containsExactly(106L);
    }

    @Test
    void listAdminOrdersAppliesCustomerFilter() {
        AsoOrder matching = sampleOrder();
        matching.setId(117L);
        matching.setCustomerId(10L);
        orderRepository.orders.put(117L, matching);

        AsoOrder otherCustomer = sampleOrder();
        otherCustomer.setId(118L);
        otherCustomer.setCustomerId(11L);
        orderRepository.orders.put(118L, otherCustomer);

        List<AsoOrder> result = orderService.listAdminOrders(new OrderQuery(
                null,
                null,
                null,
                10L,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(result).extracting(AsoOrder::getId).containsExactly(117L);
    }

    @Test
    void listAdminOrdersAttachesCustomerIdentity() {
        AsoOrder order = sampleOrder();
        order.setId(119L);
        order.setCustomerId(12L);
        orderRepository.orders.put(119L, order);

        CustomerAccount customer = new CustomerAccount();
        customer.setId(12L);
        customer.setUsername("alice");
        customer.setEmail("alice@example.com");
        customerAccountRepository.customers.put(12L, customer);

        List<AsoOrder> result = orderService.listAdminOrders(new OrderQuery(null, null));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerUsername()).isEqualTo("alice");
        assertThat(result.get(0).getCustomerEmail()).isEqualTo("alice@example.com");
    }

    private static LocalDateTime expectedNow() {
        return LocalDateTime.ofInstant(CLOCK.instant(), BUSINESS_ZONE);
    }

    private AsoOrder sampleOrder() {
        AsoOrder order = new AsoOrder();
        order.setCustomerId(10L);
        order.setCustomerAppId(1L);
        order.setOrderNo("YO202606200001");
        order.setOrderType(OrderType.DOWNLOAD);
        order.setStoreType(StoreType.APP_STORE);
        order.setRegionCode("US");
        order.setAppIdentifier("123456");
        order.setAppName("Example App");
        order.setStatus(OrderStatus.PENDING_CONFIRM);
        order.setOrderStartDate(LocalDate.of(2026, 6, 19));
        order.setOrderEndDate(LocalDate.of(2026, 6, 21));
        order.setTotalDays(3);
        order.setQuantity(60);
        order.setTotalAmount(new BigDecimal("30.00"));
        order.setBalanceBefore(new BigDecimal("100.00"));
        order.setBalanceAfter(new BigDecimal("70.00"));
        return order;
    }

    private static final class FakeCustomerAppRepository implements CustomerAppRepository {
        private final CustomerApp app;
        private List<String> linkedRegions = List.of("US", "JP", "CN");

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
        public boolean existsRegion(Long customerAppId, String regionCode) {
            return app.getId().equals(customerAppId) && linkedRegions.contains(regionCode);
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

    private static final class FakeCustomerAccountRepository implements CustomerAccountRepository {
        private final Map<Long, CustomerAccount> customers = new java.util.HashMap<>();

        private FakeCustomerAccountRepository() {
            CustomerAccount customer = new CustomerAccount();
            customer.setId(10L);
            customer.setUsername("customer");
            customer.setEmail("customer@example.com");
            customers.put(customer.getId(), customer);
        }

        @Override
        public boolean existsByUsername(String username) {
            return customers.values().stream().anyMatch(customer -> username.equals(customer.getUsername()));
        }

        @Override
        public boolean existsByEmail(String email) {
            return customers.values().stream().anyMatch(customer -> email.equals(customer.getEmail()));
        }

        @Override
        public Optional<CustomerAccount> findById(Long id) {
            return Optional.ofNullable(customers.get(id));
        }

        @Override
        public List<CustomerAccount> findByIds(List<Long> ids) {
            if (ids == null || ids.isEmpty()) {
                return List.of();
            }
            return ids.stream()
                    .distinct()
                    .map(customers::get)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }

        @Override
        public Optional<CustomerAccount> findByUsernameOrEmail(String account) {
            return customers.values().stream()
                    .filter(customer -> account.equals(customer.getUsername()) || account.equals(customer.getEmail()))
                    .findFirst();
        }

        @Override
        public List<CustomerAccount> findAll(String keyword) {
            return List.copyOf(customers.values());
        }

        @Override
        public CustomerAccount save(CustomerAccount customer) {
            customers.put(customer.getId(), customer);
            return customer;
        }

        @Override
        public void updateStatus(Long id, AccountStatus status) {
            Optional.ofNullable(customers.get(id)).ifPresent(customer -> customer.setStatus(status));
        }

        @Override
        public void updatePassword(Long id, String passwordHash, boolean forcePasswordChange) {
            Optional.ofNullable(customers.get(id)).ifPresent(customer -> {
                customer.setPasswordHash(passwordHash);
                customer.setForcePasswordChange(forcePasswordChange);
            });
        }
    }

    private static final class FakeMarketRegionRepository implements MarketRegionRepository {
        @Override
        public Optional<MarketRegion> findByCode(String code) {
            if (!List.of("US", "JP", "CN").contains(code)) {
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

    private static final class FakePricingConfigRepository implements PricingConfigRepository {
        private final Map<PriceCode, PricingConfig> prices = new EnumMap<>(PriceCode.class);

        private FakePricingConfigRepository() {
            put(PriceCode.KEYWORD_INSTALL, "1.20");
            put(PriceCode.DOWNLOAD, "0.50");
            put(PriceCode.RATING_5, "2.00");
            put(PriceCode.RATING_4, "1.50");
            put(PriceCode.REVIEW_5, "3.00");
            put(PriceCode.REVIEW_4, "2.50");
        }

        private void put(PriceCode code, String price) {
            prices.put(code, PricingConfig.enabled(code, new BigDecimal(price)));
        }

        private void put(PriceCode code, String price, String chinaPrice) {
            PricingConfig config = PricingConfig.enabled(code, new BigDecimal(price));
            config.setChinaUnitPrice(new BigDecimal(chinaPrice));
            prices.put(code, config);
        }

        @Override
        public List<PricingConfig> findAll() {
            return List.copyOf(prices.values());
        }

        @Override
        public Optional<PricingConfig> findByCode(PriceCode code) {
            return Optional.ofNullable(prices.get(code));
        }

        @Override
        public void saveAll(List<PricingConfig> configs) {
        }
    }

    private static final class FakeOrderModuleConfigRepository implements OrderModuleConfigRepository {
        private final OrderModuleConfig module = new OrderModuleConfig(
                99L, "关键词安装（高级）", "Advanced keyword installs", "Расширенная установка", "Instalação avançada", "Instalación avanzada",
                "description", "description", "description", "description", "description",
                OrderType.KEYWORD_INSTALL, new BigDecimal("10.00"), new BigDecimal("12.00"), true, 2
        );

        @Override public List<OrderModuleConfig> findAll() { return List.of(module); }
        @Override public List<OrderModuleConfig> findEnabled(OrderType orderType) { return orderType == module.orderType() ? List.of(module) : List.of(); }
        @Override public Optional<OrderModuleConfig> findById(Long id) { return module.id().equals(id) ? Optional.of(module) : Optional.empty(); }
        @Override public OrderModuleConfig save(OrderModuleConfig value) { return value; }
        @Override public void deleteById(Long id) { }
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
        public List<AsoOrder> findByCustomerId(Long customerId, OrderQuery query) {
            return orders.values().stream()
                    .filter(order -> customerId.equals(order.getCustomerId()))
                    .filter(order -> matchesQuery(order, query))
                    .sorted(java.util.Comparator.comparing(AsoOrder::getId))
                    .toList();
        }

        @Override
        public List<AsoOrder> findAll(OrderQuery query) {
            return orders.values().stream()
                    .filter(order -> matchesQuery(order, query))
                    .sorted(java.util.Comparator.comparing(AsoOrder::getId))
                    .toList();
        }

        @Override
        public List<AsoOrder> findDueBefore(LocalDateTime now) {
            return orders.values().stream()
                    .filter(order -> OrderStatus.EXECUTING.equals(order.getStatus()) || OrderStatus.PAUSED.equals(order.getStatus()) || OrderStatus.PENDING_EXECUTION.equals(order.getStatus()))
                    .filter(order -> order.getExpectedCompletedAt() != null && !order.getExpectedCompletedAt().isAfter(now))
                    .sorted(java.util.Comparator.comparing(AsoOrder::getId))
                    .toList();
        }

        private boolean matchesQuery(AsoOrder order, OrderQuery query) {
            if (query == null) {
                return true;
            }
            if (query.storeType() != null && !query.storeType().equals(order.getStoreType())) {
                return false;
            }
            if (query.status() != null && !query.status().equals(order.getStatus())) {
                return false;
            }
            if (query.keyword() != null && !query.keyword().isBlank()) {
                String keyword = query.keyword().toLowerCase();
                String content = (order.getOrderNo() + " " + order.getAppIdentifier() + " " + order.getAppName()).toLowerCase();
                if (!content.contains(keyword)) {
                    return false;
                }
            }
            if (query.customerId() != null && !query.customerId().equals(order.getCustomerId())) {
                return false;
            }
            if (query.customerAppId() != null && !query.customerAppId().equals(order.getCustomerAppId())) {
                return false;
            }
            if (query.regionCode() != null && !query.regionCode().equalsIgnoreCase(order.getRegionCode())) {
                return false;
            }
            if (query.orderType() != null && !query.orderType().equals(order.getOrderType())) {
                return false;
            }
            if (query.specialOrder() != null) {
                boolean special = order.getSourceAuditId() != null;
                if (!query.specialOrder().equals(special)) {
                    return false;
                }
            }
            if (query.orderDateFrom() != null && order.getOrderStartDate().isBefore(query.orderDateFrom())) {
                return false;
            }
            if (query.orderDateTo() != null && order.getOrderEndDate().isAfter(query.orderDateTo())) {
                return false;
            }
            if (query.createdDateFrom() != null && order.getCreatedAt().toLocalDate().isBefore(query.createdDateFrom())) {
                return false;
            }
        return query.createdDateTo() == null || !order.getCreatedAt().toLocalDate().isAfter(query.createdDateTo());
        }
    }

    private static final class FakeOrderEventRepository implements OrderEventRepository {
        private final Map<Long, List<OrderEvent>> eventsByOrderId = new java.util.HashMap<>();

        @Override
        public void save(OrderEvent event) {
            event.setCreatedAt(expectedNow());
            eventsByOrderId.computeIfAbsent(event.getOrderId(), ignored -> new ArrayList<>()).add(event);
        }

        @Override
        public List<OrderEvent> findByOrderId(Long orderId) {
            return List.copyOf(eventsByOrderId.getOrDefault(orderId, List.of()));
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

    private static final class FakeOrderCommentDetailRepository implements OrderCommentDetailRepository {
        private final Map<Long, List<OrderCommentDetail>> detailsByOrderId = new java.util.HashMap<>();

        @Override
        public void saveAll(Long orderId, List<OrderCommentDetail> details) {
            details.forEach(detail -> detail.setOrderId(orderId));
            detailsByOrderId.put(orderId, new ArrayList<>(details));
        }

        @Override
        public void deleteByOrderId(Long orderId) {
            detailsByOrderId.remove(orderId);
        }

        @Override
        public Map<Long, List<OrderCommentDetail>> findByOrderIds(List<Long> orderIds) {
            Map<Long, List<OrderCommentDetail>> result = new java.util.HashMap<>();
            orderIds.forEach(orderId -> result.put(orderId, new ArrayList<>(detailsByOrderId.getOrDefault(orderId, List.of()))));
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
