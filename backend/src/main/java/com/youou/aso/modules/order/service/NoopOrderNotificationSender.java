package com.youou.aso.modules.order.service;

import com.youou.aso.modules.order.domain.AsoOrder;

public class NoopOrderNotificationSender implements OrderNotificationSender {
    @Override
    public void notifyOrderCreated(AsoOrder order) {
        // Notification is optional; missing mail configuration must not block order creation.
    }
}
