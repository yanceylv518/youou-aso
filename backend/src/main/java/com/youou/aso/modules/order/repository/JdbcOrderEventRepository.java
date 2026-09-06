package com.youou.aso.modules.order.repository;

import com.youou.aso.modules.order.domain.OrderEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcOrderEventRepository implements OrderEventRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcOrderEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(OrderEvent event) {
        jdbcTemplate.update(
                """
                        INSERT INTO aso_order_event
                        (order_id, event_type, quantity_before, quantity_after, completed_before, completed_after,
                         amount_before, amount_after, created_by_admin_id, created_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                        """,
                event.getOrderId(),
                event.getEventType(),
                event.getQuantityBefore(),
                event.getQuantityAfter(),
                event.getCompletedBefore(),
                event.getCompletedAfter(),
                event.getAmountBefore(),
                event.getAmountAfter(),
                event.getCreatedByAdminId()
        );
    }

    @Override
    public List<OrderEvent> findByOrderId(Long orderId) {
        return jdbcTemplate.query(
                "SELECT * FROM aso_order_event WHERE order_id = ? ORDER BY created_at ASC, id ASC",
                (rs, rowNum) -> {
                    OrderEvent event = new OrderEvent();
                    event.setId(rs.getLong("id"));
                    event.setOrderId(rs.getLong("order_id"));
                    event.setEventType(rs.getString("event_type"));
                    event.setQuantityBefore((Integer) rs.getObject("quantity_before"));
                    event.setQuantityAfter((Integer) rs.getObject("quantity_after"));
                    event.setCompletedBefore((Integer) rs.getObject("completed_before"));
                    event.setCompletedAfter((Integer) rs.getObject("completed_after"));
                    event.setAmountBefore(rs.getBigDecimal("amount_before"));
                    event.setAmountAfter(rs.getBigDecimal("amount_after"));
                    event.setCreatedByAdminId((Long) rs.getObject("created_by_admin_id"));
                    event.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return event;
                },
                orderId
        );
    }
}
