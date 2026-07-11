package com.youou.aso.modules.order.job;

import com.youou.aso.modules.order.service.OrderService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderCompletionSchedulerTest {
    @Test
    void scheduledRunCompletesExpiredExecutingOrders() {
        OrderService orderService = mock(OrderService.class);
        when(orderService.completeExpiredExecutingOrders()).thenReturn(2);
        OrderCompletionScheduler scheduler = new OrderCompletionScheduler(orderService);

        scheduler.completeExpiredExecutingOrders();

        verify(orderService).completeExpiredExecutingOrders();
    }
}
