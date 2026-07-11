package com.youou.aso.modules.order.repository;

import com.youou.aso.modules.order.domain.OrderCommentDetail;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcOrderCommentDetailRepository implements OrderCommentDetailRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<OrderCommentDetail> rowMapper = (rs, rowNum) -> {
        OrderCommentDetail detail = new OrderCommentDetail();
        detail.setId(rs.getLong("id"));
        detail.setOrderId(rs.getLong("order_id"));
        detail.setRegionCode(rs.getString("region_code"));
        detail.setStarLevel(rs.getInt("star_level"));
        detail.setCommentTitle(rs.getString("comment_title"));
        detail.setCommentContent(rs.getString("comment_content"));
        detail.setCreatedAt(readDateTime(rs.getTimestamp("created_at")));
        return detail;
    };

    public JdbcOrderCommentDetailRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveAll(Long orderId, List<OrderCommentDetail> details) {
        if (orderId == null || details == null || details.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(
                """
                        INSERT INTO aso_order_comment_detail
                        (order_id, region_code, star_level, comment_title, comment_content)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                details,
                details.size(),
                (ps, detail) -> {
                    ps.setLong(1, orderId);
                    ps.setString(2, detail.getRegionCode());
                    ps.setInt(3, detail.getStarLevel());
                    ps.setString(4, detail.getCommentTitle());
                    ps.setString(5, detail.getCommentContent());
                }
        );
    }

    @Override
    public void deleteByOrderId(Long orderId) {
        if (orderId == null) {
            return;
        }
        jdbcTemplate.update("DELETE FROM aso_order_comment_detail WHERE order_id = ?", orderId);
    }

    @Override
    public Map<Long, List<OrderCommentDetail>> findByOrderIds(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = String.join(",", orderIds.stream().map(id -> "?").toList());
        List<OrderCommentDetail> details = jdbcTemplate.query(
                "SELECT * FROM aso_order_comment_detail WHERE order_id IN (" + placeholders + ") ORDER BY id ASC",
                rowMapper,
                orderIds.toArray()
        );
        Map<Long, List<OrderCommentDetail>> result = new LinkedHashMap<>();
        for (OrderCommentDetail detail : details) {
            result.computeIfAbsent(detail.getOrderId(), ignored -> new java.util.ArrayList<>()).add(detail);
        }
        return result;
    }

    private static LocalDateTime readDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
