package com.youou.aso.modules.order.repository;

import com.youou.aso.modules.order.domain.AsoOrder;
import com.youou.aso.modules.order.dto.OrderQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    AsoOrder save(AsoOrder order);

    Optional<AsoOrder> findById(Long id);

    AsoOrder update(AsoOrder order);

    AsoOrder updatePaymentDraft(AsoOrder order);

    List<AsoOrder> findByCustomerId(Long customerId, OrderQuery query);

    default List<AsoOrder> findByCustomerId(Long customerId, OrderQuery query, int limit, int offset) {
        return findByCustomerId(customerId, query).stream().skip(offset).limit(limit).toList();
    }

    default long countByCustomerId(Long customerId, OrderQuery query) {
        return findByCustomerId(customerId, query).size();
    }

    List<AsoOrder> findAll(OrderQuery query);

    default List<AsoOrder> findAll(OrderQuery query, int limit, int offset) {
        return findAll(query).stream().skip(offset).limit(limit).toList();
    }

    default long countAll(OrderQuery query) {
        return findAll(query).size();
    }

    List<AsoOrder> findExecutingDueBefore(LocalDateTime now);
}
