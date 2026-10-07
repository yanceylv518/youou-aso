package com.youou.aso.modules.order.repository;

import com.youou.aso.modules.order.domain.SpecialOrderAuditItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcSpecialOrderAuditItemRepository implements SpecialOrderAuditItemRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<SpecialOrderAuditItem> rowMapper = (rs, rowNum) -> {
        SpecialOrderAuditItem item = new SpecialOrderAuditItem();
        item.setId(rs.getLong("id"));
        item.setAuditId(rs.getLong("audit_id"));
        item.setRegionCode(rs.getString("region_code"));
        item.setKeyword(rs.getString("keyword"));
        item.setChartType(rs.getString("chart_type"));
        item.setTargetRank(readInteger(rs, "target_rank"));
        item.setCoverageNote(rs.getString("coverage_note"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setExecutionDays(readInteger(rs, "execution_days"));
        item.setCreatedAt(readDateTime(rs.getTimestamp("created_at")));
        return item;
    };

    public JdbcSpecialOrderAuditItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void deleteByAuditId(Long auditId) {
        jdbcTemplate.update("DELETE FROM special_order_audit_item WHERE audit_id = ?", auditId);
    }

    @Override
    public void saveAll(Long auditId, List<SpecialOrderAuditItem> items) {
        if (auditId == null || items == null || items.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(
                """
                        INSERT INTO special_order_audit_item
                        (audit_id, region_code, keyword, chart_type, target_rank, coverage_note, unit_price, execution_days)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                items,
                items.size(),
                (ps, item) -> {
                    ps.setLong(1, auditId);
                    ps.setString(2, item.getRegionCode());
                    ps.setString(3, item.getKeyword());
                    ps.setString(4, item.getChartType());
                    if (item.getTargetRank() == null) {
                        ps.setObject(5, null);
                    } else {
                        ps.setInt(5, item.getTargetRank());
                    }
                    ps.setString(6, item.getCoverageNote());
                    ps.setBigDecimal(7, item.getUnitPrice());
                    ps.setObject(8, item.getExecutionDays());
                }
        );
    }

    @Override
    public void updatePricing(Long id, java.math.BigDecimal unitPrice, Integer executionDays) {
        jdbcTemplate.update("UPDATE special_order_audit_item SET unit_price = ?, execution_days = ? WHERE id = ?",
                unitPrice, executionDays, id);
    }

    @Override
    public Map<Long, List<SpecialOrderAuditItem>> findByAuditIds(List<Long> auditIds) {
        if (auditIds == null || auditIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = String.join(",", auditIds.stream().map(id -> "?").toList());
        List<SpecialOrderAuditItem> items = jdbcTemplate.query(
                "SELECT * FROM special_order_audit_item WHERE audit_id IN (" + placeholders + ") ORDER BY id ASC",
                rowMapper,
                auditIds.toArray()
        );
        Map<Long, List<SpecialOrderAuditItem>> result = new LinkedHashMap<>();
        for (SpecialOrderAuditItem item : items) {
            result.computeIfAbsent(item.getAuditId(), ignored -> new java.util.ArrayList<>()).add(item);
        }
        return result;
    }

    private static Integer readInteger(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDateTime readDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
