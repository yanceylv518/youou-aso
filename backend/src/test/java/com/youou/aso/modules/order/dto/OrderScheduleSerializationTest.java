package com.youou.aso.modules.order.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.order.api.AdminOrderController;
import com.youou.aso.modules.order.domain.AsoOrder;
import com.youou.aso.modules.order.domain.OrderStatus;
import com.youou.aso.modules.order.service.OrderService;
import com.youou.aso.modules.order.service.SpecialOrderAuditService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderScheduleSerializationTest {
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void customerControllerForwardsScheduledMinuteForCreateAndResubmit() throws Exception {
        var request = mapper.readValue("""
                {"customerAppId":1,"orderType":"KEYWORD_INSTALL","startDate":"2026-09-25",
                 "endDate":"2026-09-25","scheduledStartAt":"2026-09-25T14:35","executionHours":1,"orderModuleId":2}
                """, com.youou.aso.modules.order.api.CustomerOrderController.CreateOrderRequest.class);
        OrderService service = mock(OrderService.class);
        AsoOrder order = new AsoOrder();
        order.setStatus(OrderStatus.PENDING_CONFIRM);
        when(service.createCustomerOrder(eq(2L), any())).thenReturn(order);
        when(service.resubmitEditableOrder(eq(2L), eq(3L), any())).thenReturn(order);
        var controller = new com.youou.aso.modules.order.api.CustomerOrderController(service);
        var account = new AuthenticatedAccount(2L, "CUSTOMER", "");
        controller.create(account, request);
        controller.resubmit(account, 3L, request);
        ArgumentCaptor<CreateOrderCommand> command = ArgumentCaptor.forClass(CreateOrderCommand.class);
        verify(service).createCustomerOrder(eq(2L), command.capture());
        verify(service).resubmitEditableOrder(eq(2L), eq(3L), command.capture());
        assertThat(command.getAllValues()).allSatisfy(value ->
            assertThat(value.scheduledStartAt()).isEqualTo(LocalDateTime.of(2026, 9, 25, 14, 35)));
    }

    @Test
    void customerPayloadAcceptsMinutePrecisionWithoutSeconds() throws Exception {
        CreateOrderCommand command = mapper.readValue("""
                {"customerAppId":1,"orderType":"KEYWORD_INSTALL","startDate":"2026-09-25",
                 "endDate":"2026-09-25","scheduledStartAt":"2026-09-25T14:35","executionHours":1}
                """, CreateOrderCommand.class);
        assertThat(command.scheduledStartAt()).isEqualTo(LocalDateTime.of(2026, 9, 25, 14, 35));
    }

    @Test
    void administratorPayloadForwardsScheduledMinuteToOrderService() throws Exception {
        AdminOrderController.AdminCreateOrderRequest request = mapper.readValue("""
                {"customerId":2,"customerAppId":1,"orderType":"KEYWORD_INSTALL","startDate":"2026-09-19",
                 "endDate":"2026-09-19","scheduledStartAt":"2026-09-19T14:35","executionHours":1}
                """, AdminOrderController.AdminCreateOrderRequest.class);
        OrderService service = mock(OrderService.class);
        AsoOrder order = new AsoOrder();
        order.setStatus(OrderStatus.PENDING_EXECUTION);
        when(service.createAdminOrderForCustomer(eq(2L), eq(7L), any())).thenReturn(order);
        AdminOrderController controller = new AdminOrderController(service, mock(SpecialOrderAuditService.class));
        controller.createForCustomer(new AuthenticatedAccount(7L, AccountType.ADMIN.name(), "ADMIN"), request);
        ArgumentCaptor<CreateOrderCommand> command = ArgumentCaptor.forClass(CreateOrderCommand.class);
        verify(service).createAdminOrderForCustomer(eq(2L), eq(7L), command.capture());
        assertThat(command.getValue().scheduledStartAt()).isEqualTo(LocalDateTime.of(2026, 9, 19, 14, 35));
    }
}
