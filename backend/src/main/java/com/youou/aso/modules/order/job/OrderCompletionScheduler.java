package com.youou.aso.modules.order.job;

import com.youou.aso.modules.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderCompletionScheduler {
    private static final Logger log = LoggerFactory.getLogger(OrderCompletionScheduler.class);

    private final OrderService orderService;

    public OrderCompletionScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(
            initialDelayString = "${youou.order.completion.initial-delay-ms:60000}",
            fixedDelayString = "${youou.order.completion.fixed-delay-ms:60000}"
    )
    public void completeExpiredExecutingOrders() {
        try {
            int completedCount = orderService.completeExpiredExecutingOrders();
            if (completedCount > 0) {
                log.info("Completed {} expired executing orders", completedCount);
            }
        } catch (Exception ex) {
            log.error("Failed to complete expired executing orders", ex);
        }
    }
}
