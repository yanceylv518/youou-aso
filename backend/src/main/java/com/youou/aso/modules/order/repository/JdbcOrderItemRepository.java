package com.youou.aso.modules.order.repository;

import com.youou.aso.modules.order.domain.OrderItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcOrderItemRepository implements OrderItemRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<OrderItem> rowMapper = (rs, rowNum) -> {
        OrderItem item = new OrderItem();
        item.setId(rs.getLong("id"));
        item.setOrderId(rs.getLong("order_id"));
        item.setItemType(rs.getString("item_type"));
        item.setItemName(rs.getString("item_name"));
        item.setRegionCode(rs.getString("region_code"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setAmount(rs.getBigDecimal("amount"));
        item.setMetadataJson(rs.getString("metadata_json"));
        item.setCreatedAt(readDateTime(rs.getTimestamp("created_at")));
        return item;
    };

    public JdbcOrderItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveAll(Long orderId, List<OrderItem> items) {
        if (orderId == null || items == null || items.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(
                """
                        INSERT INTO aso_order_item
                        (order_id, item_type, item_name, region_code, quantity, unit_price, amount, metadata_json)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                items,
                items.size(),
                (ps, item) -> {
                    ps.setLong(1, orderId);
                    ps.setString(2, item.getItemType());
                    ps.setString(3, item.getItemName());
                    ps.setString(4, item.getRegionCode());
                    ps.setInt(5, item.getQuantity());
                    ps.setBigDecimal(6, item.getUnitPrice());
                    ps.setBigDecimal(7, item.getAmount());
                    ps.setString(8, item.getMetadataJson());
                }
        );
    }

    @Override
    public void deleteByOrderId(Long orderId) {
        if (orderId == null) {
            return;
        }
        jdbcTemplate.update("DELETE FROM aso_order_item WHERE order_id = ?", orderId);
    }

    @Override
    public Map<Long, List<OrderItem>> findByOrderIds(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = String.join(",", orderIds.stream().map(id -> "?").toList());
        List<OrderItem> items = jdbcTemplate.query(
                "SELECT * FROM aso_order_item WHERE order_id IN (" + placeholders + ") ORDER BY id ASC",
                rowMapper,
                orderIds.toArray()
        );
        Map<Long, List<OrderItem>> result = new LinkedHashMap<>();
        for (OrderItem item : items) {
            result.computeIfAbsent(item.getOrderId(), ignored -> new java.util.ArrayList<>()).add(item);
        }
        return result;
    }

    private static LocalDateTime readDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
