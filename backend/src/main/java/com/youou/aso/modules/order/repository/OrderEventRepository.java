package com.youou.aso.modules.order.repository;

import com.youou.aso.modules.order.domain.OrderEvent;

import java.util.List;

public interface OrderEventRepository {
    void save(OrderEvent event);
    List<OrderEvent> findByOrderId(Long orderId);
}