package com.youou.aso.modules.order.repository;

import com.youou.aso.modules.order.domain.OrderItem;

import java.util.List;
import java.util.Map;

public interface OrderItemRepository {
    void saveAll(Long orderId, List<OrderItem> items);

    void deleteByOrderId(Long orderId);

    default void updateCompletedQuantities(List<OrderItem> items) {
    }

    default void updateQuantitiesAndProgress(List<OrderItem> items) {
    }

    Map<Long, List<OrderItem>> findByOrderIds(List<Long> orderIds);
}
