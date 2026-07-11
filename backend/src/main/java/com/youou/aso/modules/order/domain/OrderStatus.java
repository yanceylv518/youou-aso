package com.youou.aso.modules.order.domain;

public enum OrderStatus {
    PENDING_PAYMENT,
    PENDING_CONFIRM,
    PENDING_EXECUTION,
    EXECUTING,
    PAUSED,
    COMPLETED,
    CANCELLED
}
