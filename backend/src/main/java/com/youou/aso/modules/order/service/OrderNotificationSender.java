package com.youou.aso.modules.order.service;

import com.youou.aso.modules.order.domain.AsoOrder;

public interface OrderNotificationSender {
    void notifyOrderCreated(AsoOrder order);
}
