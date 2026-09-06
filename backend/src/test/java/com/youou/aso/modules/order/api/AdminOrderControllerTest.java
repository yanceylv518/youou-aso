package com.youou.aso.modules.order.api;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.order.domain.AsoOrder;
import com.youou.aso.modules.order.domain.OrderStatus;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.order.domain.SpecialAuditStatus;
import com.youou.aso.modules.order.domain.SpecialOrderAudit;
import com.youou.aso.modules.order.dto.AdminCreateOrderResult;
import com.youou.aso.modules.order.dto.OrderResult;
import com.youou.aso.modules.order.service.OrderService;
import com.youou.aso.modules.order.service.SpecialOrderAuditService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminOrderControllerTest {
    private final OrderService orderService = mock(OrderService.class);
    private final SpecialOrderAuditService specialOrderAuditService = mock(SpecialOrderAuditService.class);
    private final AdminOrderController controller = new AdminOrderController(orderService, specialOrderAuditService);

    @Test
    void adminCanCreateOrderForCustomer() {
        AsoOrder created = sampleOrder();
        when(orderService.createAdminOrderForCustomer(
                20L,
                1L,
                new com.youou.aso.modules.order.dto.CreateOrderCommand(
                        30L,
                        OrderType.DOWNLOAD,
                        LocalDate.of(2026, 6, 20),
                        LocalDate.of(2026, 6, 21),
                        null,
                        List.of(),
                        null,
                        100,
                        null,
                        null,
                        null,
                        null
                )
        )).thenReturn(created);

        AdminCreateOrderResult result = controller.createForCustomer(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "ADMIN"),
                new AdminOrderController.AdminCreateOrderRequest(
                        20L,
                        30L,
                        OrderType.DOWNLOAD,
                        LocalDate.of(2026, 6, 20),
                        LocalDate.of(2026, 6, 21),
                        null,
                        List.of(),
                        null,
                        100,
                        null,
                        null,
                        null,
                        null
                )
        ).data();

        assertThat(result.paid()).isTrue();
        assertThat(result.waitPayment()).isFalse();
        assertThat(result.order()).isNotNull();
        assertThat(result.order().customerId()).isEqualTo(20L);
        verify(orderService).createAdminOrderForCustomer(
                20L,
                1L,
                new com.youou.aso.modules.order.dto.CreateOrderCommand(
                        30L,
                        OrderType.DOWNLOAD,
                        LocalDate.of(2026, 6, 20),
                        LocalDate.of(2026, 6, 21),
                        null,
                        List.of(),
                        100,
                        null,
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void adminSpecialOrderCanWaitForPaymentWhenBalanceIsInsufficient() {
        SpecialOrderAudit audit = sampleAudit();
        when(specialOrderAuditService.submitAdminSpecialOrder(
                20L,
                1L,
                30L,
                "US",
                OrderType.RANK_GUARANTEE,
                List.of(),
                "Telegram",
                "@youou",
                new BigDecimal("99.00")
        )).thenReturn(new SpecialOrderAuditService.AdminSpecialOrderSubmission(null, audit, false));

        AdminCreateOrderResult result = controller.createForCustomer(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "ADMIN"),
                new AdminOrderController.AdminCreateOrderRequest(
                        20L,
                        30L,
                        "US",
                        OrderType.RANK_GUARANTEE,
                        LocalDate.of(2026, 6, 20),
                        LocalDate.of(2026, 6, 20),
                        null,
                        "Telegram",
                        "@youou",
                        null,
                        List.of(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        List.of(),
                        new BigDecimal("99.00")
                )
        ).data();

        assertThat(result.paid()).isFalse();
        assertThat(result.waitPayment()).isTrue();
        assertThat(result.audit()).isNotNull();
        assertThat(result.audit().status()).isEqualTo(SpecialAuditStatus.APPROVED_WAIT_SUBMIT);
    }

    @Test
    void adminCanCancelOrderWithReason() {
        AsoOrder cancelled = sampleOrder();
        cancelled.setStatus(OrderStatus.CANCELLED);
        cancelled.setRejectReason("Customer requested cancellation");
        cancelled.setRefundTransactionId(9L);
        when(orderService.cancelOrder(10L, 1L, "Customer requested cancellation")).thenReturn(cancelled);

        OrderResult result = controller.cancel(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "SUPER_ADMIN"),
                10L,
                new AdminOrderController.CancelOrderRequest("Customer requested cancellation")
        ).data();

        assertThat(result.status()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderService).cancelOrder(10L, 1L, "Customer requested cancellation");
    }

    @Test
    void adminCanPauseExecutingOrder() {
        AsoOrder paused = sampleOrder();
        paused.setStatus(OrderStatus.PAUSED);
        when(orderService.pauseOrder(10L, 1L)).thenReturn(paused);

        OrderResult result = controller.pause(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "SUPER_ADMIN"),
                10L
        ).data();

        assertThat(result.status()).isEqualTo(OrderStatus.PAUSED);
        verify(orderService).pauseOrder(10L, 1L);
    }

    @Test
    void adminCanResumePausedOrder() {
        AsoOrder resumed = sampleOrder();
        resumed.setStatus(OrderStatus.EXECUTING);
        when(orderService.resumeOrder(10L, 1L)).thenReturn(resumed);

        OrderResult result = controller.resume(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "SUPER_ADMIN"),
                10L
        ).data();

        assertThat(result.status()).isEqualTo(OrderStatus.EXECUTING);
        verify(orderService).resumeOrder(10L, 1L);
    }

    @Test
    void adminCanBatchPauseExecutingOrders() {
        AsoOrder first = sampleOrder();
        first.setStatus(OrderStatus.PAUSED);
        AsoOrder second = sampleOrder();
        second.setId(11L);
        second.setStatus(OrderStatus.PAUSED);
        when(orderService.pauseOrders(List.of(10L, 11L), 1L)).thenReturn(List.of(first, second));

        List<OrderResult> result = controller.batchPause(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "SUPER_ADMIN"),
                new AdminOrderController.BatchExecuteRequest(List.of(10L, 11L))
        ).data();

        assertThat(result).extracting(OrderResult::status).containsExactly(OrderStatus.PAUSED, OrderStatus.PAUSED);
        verify(orderService).pauseOrders(List.of(10L, 11L), 1L);
    }

    @Test
    void adminCanBatchConfirmPendingOrders() {
        AsoOrder first = sampleOrder();
        first.setStatus(OrderStatus.PENDING_EXECUTION);
        AsoOrder second = sampleOrder();
        second.setId(11L);
        second.setStatus(OrderStatus.PENDING_EXECUTION);
        when(orderService.confirmOrders(List.of(10L, 11L), 1L)).thenReturn(List.of(first, second));

        List<OrderResult> result = controller.batchConfirm(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "SUPER_ADMIN"),
                new AdminOrderController.BatchExecuteRequest(List.of(10L, 11L))
        ).data();

        assertThat(result).extracting(OrderResult::status).containsExactly(OrderStatus.PENDING_EXECUTION, OrderStatus.PENDING_EXECUTION);
        verify(orderService).confirmOrders(List.of(10L, 11L), 1L);
    }

    @Test
    void adminCanViewOrderDetail() {
        AsoOrder order = sampleOrder();
        when(orderService.getAdminOrder(10L)).thenReturn(order);

        OrderResult result = controller.detail(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "SUPER_ADMIN"),
                10L
        ).data();

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.orderNo()).isEqualTo("YO202606200001");
        verify(orderService).getAdminOrder(10L);
    }

    @Test
    void customerCannotCancelOrderFromAdminEndpoint() {
        assertThatThrownBy(() -> controller.cancel(
                new AuthenticatedAccount(10L, AccountType.CUSTOMER.name(), "CUSTOMER"),
                10L,
                new AdminOrderController.CancelOrderRequest("No longer needed")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    private AsoOrder sampleOrder() {
        AsoOrder order = new AsoOrder();
        order.setId(10L);
        order.setOrderNo("YO202606200001");
        order.setCustomerId(20L);
        order.setCustomerAppId(30L);
        order.setOrderType(OrderType.DOWNLOAD);
        order.setStoreType(StoreType.APP_STORE);
        order.setRegionCode("US");
        order.setAppIdentifier("123456");
        order.setAppName("Example App");
        order.setStatus(OrderStatus.PENDING_EXECUTION);
        order.setOrderStartDate(LocalDate.of(2026, 6, 20));
        order.setOrderEndDate(LocalDate.of(2026, 6, 20));
        order.setTotalDays(1);
        order.setQuantity(10);
        order.setTotalAmount(new BigDecimal("5.00"));
        order.setItems(List.of());
        return order;
    }

    private SpecialOrderAudit sampleAudit() {
        SpecialOrderAudit audit = new SpecialOrderAudit();
        audit.setId(50L);
        audit.setAuditNo("SA202606200001");
        audit.setCustomerId(20L);
        audit.setCustomerAppId(30L);
        audit.setOrderType(OrderType.RANK_GUARANTEE);
        audit.setStoreType(StoreType.APP_STORE);
        audit.setRegionCode("US");
        audit.setAppIdentifier("123456");
        audit.setAppName("Example App");
        audit.setRequestedContent("关键词保排名服务 - Example App");
        audit.setNegotiatedContent("关键词保排名服务 - Example App");
        audit.setNegotiatedPrice(new BigDecimal("99.00"));
        audit.setStatus(SpecialAuditStatus.APPROVED_WAIT_SUBMIT);
        return audit;
    }
}
