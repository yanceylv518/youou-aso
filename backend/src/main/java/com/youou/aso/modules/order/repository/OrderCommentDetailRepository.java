package com.youou.aso.modules.order.repository;

import com.youou.aso.modules.order.domain.OrderCommentDetail;

import java.util.List;
import java.util.Map;

public interface OrderCommentDetailRepository {
    void saveAll(Long orderId, List<OrderCommentDetail> details);

    void deleteByOrderId(Long orderId);

    Map<Long, List<OrderCommentDetail>> findByOrderIds(List<Long> orderIds);
}
