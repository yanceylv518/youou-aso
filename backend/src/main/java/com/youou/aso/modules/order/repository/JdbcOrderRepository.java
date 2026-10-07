package com.youou.aso.modules.order.repository;

import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.order.domain.AsoOrder;
import com.youou.aso.modules.order.domain.OrderStatus;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.order.dto.OrderQuery;
import com.youou.aso.modules.pricing.domain.PriceCode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcOrderRepository implements OrderRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<AsoOrder> rowMapper = (rs, rowNum) -> {
        AsoOrder order = new AsoOrder();
        order.setId(rs.getLong("id"));
        order.setOrderNo(rs.getString("order_no"));
        order.setCustomerId(rs.getLong("customer_id"));
        order.setCustomerAppId(rs.getLong("customer_app_id"));
        long sourceAuditId = rs.getLong("source_audit_id");
        order.setSourceAuditId(rs.wasNull() ? null : sourceAuditId);
        order.setOrderType(OrderType.valueOf(rs.getString("order_type")));
        order.setOrderModuleId(readLong(rs, "order_module_id"));
        order.setOrderModuleName(rs.getString("order_module_name"));
        String pricingCode = rs.getString("pricing_code");
        order.setPricingCode(pricingCode == null ? null : PriceCode.valueOf(pricingCode));
        order.setStoreType(StoreType.valueOf(rs.getString("store_type")));
        order.setRegionCode(rs.getString("region_code"));
        order.setAppIdentifier(rs.getString("app_identifier"));
        order.setAppName(rs.getString("app_name"));
        order.setAppIconUrl(rs.getString("app_icon_url"));
        order.setStatus(OrderStatus.valueOf(rs.getString("status")));
        order.setOrderStartDate(rs.getDate("order_start_date") == null ? null : rs.getDate("order_start_date").toLocalDate());
        order.setOrderEndDate(rs.getDate("order_end_date") == null ? null : rs.getDate("order_end_date").toLocalDate());
        order.setScheduledStartAt(readDateTime(rs.getTimestamp("scheduled_start_at")));
        order.setExecutionHours(readInteger(rs, "execution_hours"));
        order.setTotalDays(readInteger(rs, "total_days"));
        order.setQuantity(readInteger(rs, "quantity"));
        order.setRefundAmount(rs.getBigDecimal("refund_amount"));
        order.setUnitPrice(rs.getBigDecimal("unit_price"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setBalanceBefore(rs.getBigDecimal("balance_before"));
        order.setBalanceAfter(rs.getBigDecimal("balance_after"));
        order.setDeductedTransactionId(readLong(rs, "deducted_transaction_id"));
        order.setRefundTransactionId(readLong(rs, "refund_transaction_id"));
        order.setRejectReason(rs.getString("reject_reason"));
        order.setConfirmedByAdminId(readLong(rs, "confirmed_by_admin_id"));
        order.setConfirmedAt(readDateTime(rs.getTimestamp("confirmed_at")));
        order.setExecutedByAdminId(readLong(rs, "executed_by_admin_id"));
        order.setExecutedAt(readDateTime(rs.getTimestamp("executed_at")));
        order.setExpectedCompletedAt(readDateTime(rs.getTimestamp("expected_completed_at")));
        order.setCompletedAt(readDateTime(rs.getTimestamp("completed_at")));
        order.setCreatedAt(readDateTime(rs.getTimestamp("created_at")));
        order.setUpdatedAt(readDateTime(rs.getTimestamp("updated_at")));
        return order;
    };

    public JdbcOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AsoOrder save(AsoOrder order) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                            INSERT INTO aso_order
                            (order_no, customer_id, customer_app_id, source_audit_id, order_type, order_module_id, order_module_name, pricing_code,
                             store_type, region_code, app_identifier, app_name, app_icon_url, status,
                             order_start_date, order_end_date, execution_hours, total_days, quantity, unit_price,
                             total_amount, balance_before, balance_after, deducted_transaction_id,
                             refund_transaction_id, reject_reason, confirmed_by_admin_id, confirmed_at,
                             executed_by_admin_id, executed_at, expected_completed_at, completed_at, scheduled_start_at)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS
            );
            bindSave(ps, order);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            order.setId(key.longValue());
        }
        return findById(order.getId()).orElse(order);
    }

    @Override
    public Optional<AsoOrder> findById(Long id) {
        return jdbcTemplate.query("SELECT * FROM aso_order WHERE id = ?", rowMapper, id).stream().findFirst();
    }

    @Override
    public Optional<AsoOrder> findByIdForUpdate(Long id) {
        return jdbcTemplate.query("SELECT * FROM aso_order WHERE id = ? FOR UPDATE", rowMapper, id).stream().findFirst();
    }

    @Override
    public AsoOrder update(AsoOrder order) {
        jdbcTemplate.update(
                """
                        UPDATE aso_order
                        SET status = ?, reject_reason = ?, confirmed_by_admin_id = ?, confirmed_at = ?,
                            executed_by_admin_id = ?, executed_at = ?, expected_completed_at = ?,
                            completed_at = ?, refund_transaction_id = ?, refund_amount = ?, quantity = ?, total_amount = ?,
                            balance_before = ?, balance_after = ?, deducted_transaction_id = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                order.getStatus().name(),
                order.getRejectReason(),
                order.getConfirmedByAdminId(),
                order.getConfirmedAt(),
                order.getExecutedByAdminId(),
                order.getExecutedAt(),
                order.getExpectedCompletedAt(),
                order.getCompletedAt(),
                order.getRefundTransactionId(),
                order.getRefundAmount(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getBalanceBefore(),
                order.getBalanceAfter(),
                order.getDeductedTransactionId(),
                order.getId()
        );
        return findById(order.getId()).orElse(order);
    }

    @Override
    public AsoOrder updatePaymentDraft(AsoOrder order) {
        jdbcTemplate.update(
                """
                        UPDATE aso_order
                        SET customer_app_id = ?, order_type = ?, order_module_id = ?, order_module_name = ?, pricing_code = ?, store_type = ?,
                            region_code = ?, app_identifier = ?, app_name = ?, app_icon_url = ?, status = ?,
                            order_start_date = ?, order_end_date = ?, execution_hours = ?, total_days = ?,
                            quantity = ?, unit_price = ?, total_amount = ?, balance_before = ?, balance_after = ?,
                            deducted_transaction_id = ?, expected_completed_at = ?, scheduled_start_at = ?,
                            reject_reason = ?, refund_transaction_id = ?, refund_amount = ?,
                            confirmed_by_admin_id = ?, confirmed_at = ?, executed_by_admin_id = ?, executed_at = ?, completed_at = ?,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                order.getCustomerAppId(),
                order.getOrderType().name(),
                order.getOrderModuleId(),
                order.getOrderModuleName(),
                order.getPricingCode() == null ? null : order.getPricingCode().name(),
                order.getStoreType().name(),
                order.getRegionCode(),
                order.getAppIdentifier(),
                order.getAppName(),
                order.getAppIconUrl(),
                order.getStatus().name(),
                order.getOrderStartDate(),
                order.getOrderEndDate(),
                order.getExecutionHours(),
                order.getTotalDays(),
                order.getQuantity(),
                order.getUnitPrice(),
                order.getTotalAmount(),
                order.getBalanceBefore(),
                order.getBalanceAfter(),
                order.getDeductedTransactionId(),
                order.getExpectedCompletedAt(),
                order.getScheduledStartAt(),
                order.getRejectReason(),
                order.getRefundTransactionId(),
                order.getRefundAmount(),
                order.getConfirmedByAdminId(),
                order.getConfirmedAt(),
                order.getExecutedByAdminId(),
                order.getExecutedAt(),
                order.getCompletedAt(),
                order.getId()
        );
        return findById(order.getId()).orElse(order);
    }

    @Override
    public List<AsoOrder> findByCustomerId(Long customerId, OrderQuery query) {
        QueryParts parts = buildQuery("SELECT * FROM aso_order WHERE customer_id = ?", List.of(customerId), query);
        return jdbcTemplate.query(parts.sql(), rowMapper, parts.params().toArray());
    }

    @Override
    public List<AsoOrder> findByCustomerId(Long customerId, OrderQuery query, int limit, int offset) {
        QueryParts parts = buildQuery("SELECT * FROM aso_order WHERE customer_id = ?", List.of(customerId), query);
        List<Object> params = new ArrayList<>(parts.params());
        params.add(limit);
        params.add(offset);
        return jdbcTemplate.query(parts.sql() + " LIMIT ? OFFSET ?", rowMapper, params.toArray());
    }

    @Override
    public long countByCustomerId(Long customerId, OrderQuery query) {
        QueryParts parts = buildQuery("SELECT * FROM aso_order WHERE customer_id = ?", List.of(customerId), query);
        return count(parts);
    }

    @Override
    public List<AsoOrder> findAll(OrderQuery query) {
        QueryParts parts = buildQuery("SELECT * FROM aso_order WHERE 1 = 1", List.of(), query);
        return jdbcTemplate.query(parts.sql(), rowMapper, parts.params().toArray());
    }

    @Override
    public List<AsoOrder> findAll(OrderQuery query, int limit, int offset) {
        QueryParts parts = buildQuery("SELECT * FROM aso_order WHERE 1 = 1", List.of(), query);
        List<Object> params = new ArrayList<>(parts.params());
        params.add(limit);
        params.add(offset);
        return jdbcTemplate.query(parts.sql() + " LIMIT ? OFFSET ?", rowMapper, params.toArray());
    }

    @Override
    public long countAll(OrderQuery query) {
        QueryParts parts = buildQuery("SELECT * FROM aso_order WHERE 1 = 1", List.of(), query);
        return count(parts);
    }

    @Override
    public List<AsoOrder> findDueBefore(LocalDateTime now) {
        return jdbcTemplate.query(
                """
                        SELECT o.* FROM aso_order o
                        WHERE (
                            o.status IN (?, ?)
                            OR (
                                o.status = ?
                                AND EXISTS (
                                    SELECT 1 FROM aso_order_item i
                                    WHERE i.order_id = o.id AND i.completed_quantity IS NOT NULL
                                )
                            )
                        )
                        AND o.expected_completed_at IS NOT NULL AND o.expected_completed_at <= ?
                        ORDER BY o.expected_completed_at ASC, o.id ASC
                        FOR UPDATE SKIP LOCKED
                        """,
                rowMapper,
                OrderStatus.EXECUTING.name(),
                OrderStatus.PAUSED.name(),
                OrderStatus.PENDING_EXECUTION.name(),
                now
        );
    }
    private QueryParts buildQuery(String baseSql, List<Object> baseParams, OrderQuery query) {
        StringBuilder sql = new StringBuilder(baseSql);
        List<Object> params = new ArrayList<>(baseParams);
        if (query != null && query.storeType() != null) {
            sql.append(" AND store_type = ?");
            params.add(query.storeType().name());
        }
        if (query != null && query.status() != null) {
            sql.append(" AND status = ?");
            params.add(query.status().name());
        }
        if (query != null && query.keyword() != null && !query.keyword().isBlank()) {
            String keyword = "%" + query.keyword().trim() + "%";
            sql.append(" AND (order_no LIKE ? OR app_identifier LIKE ? OR app_name LIKE ?)");
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }
        if (query != null && query.customerId() != null) {
            sql.append(" AND customer_id = ?");
            params.add(query.customerId());
        }
        if (query != null && query.customerAppId() != null) {
            sql.append(" AND customer_app_id = ?");
            params.add(query.customerAppId());
        }
        if (query != null && query.regionCode() != null && !query.regionCode().isBlank()) {
            sql.append("""
                     AND (region_code = ? OR EXISTS (
                        SELECT 1 FROM aso_order_item item
                        WHERE item.order_id = aso_order.id AND item.region_code = ?
                     ))
                    """);
            String regionCode = query.regionCode().trim().toUpperCase();
            params.add(regionCode);
            params.add(regionCode);
        }
        if (query != null && query.orderType() != null) {
            sql.append(" AND order_type = ?");
            params.add(query.orderType().name());
        }
        if (query != null && query.specialOrder() != null) {
            sql.append(query.specialOrder() ? " AND source_audit_id IS NOT NULL" : " AND source_audit_id IS NULL");
        }
        if (query != null && query.orderDateFrom() != null) {
            sql.append(" AND order_start_date >= ?");
            params.add(query.orderDateFrom());
        }
        if (query != null && query.orderDateTo() != null) {
            sql.append(" AND order_end_date <= ?");
            params.add(query.orderDateTo());
        }
        if (query != null && query.createdDateFrom() != null) {
            sql.append(" AND created_at >= ?");
            params.add(query.createdDateFrom().atStartOfDay());
        }
        if (query != null && query.createdDateTo() != null) {
            sql.append(" AND created_at < ?");
            params.add(query.createdDateTo().plusDays(1).atStartOfDay());
        }
        sql.append(" ORDER BY created_at DESC, id DESC");
        return new QueryParts(sql.toString(), params);
    }

    private long count(QueryParts parts) {
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM (" + parts.sql() + ") counted",
                Long.class,
                parts.params().toArray()
        );
        return total == null ? 0 : total;
    }

    private void bindSave(PreparedStatement ps, AsoOrder order) throws java.sql.SQLException {
        ps.setString(1, order.getOrderNo());
        ps.setLong(2, order.getCustomerId());
        ps.setLong(3, order.getCustomerAppId());
        setNullableLong(ps, 4, order.getSourceAuditId());
        ps.setString(5, order.getOrderType().name());
        setNullableLong(ps, 6, order.getOrderModuleId());
        ps.setString(7, order.getOrderModuleName());
        ps.setString(8, order.getPricingCode() == null ? null : order.getPricingCode().name());
        ps.setString(9, order.getStoreType().name());
        ps.setString(10, order.getRegionCode());
        ps.setString(11, order.getAppIdentifier());
        ps.setString(12, order.getAppName());
        ps.setString(13, order.getAppIconUrl());
        ps.setString(14, order.getStatus().name());
        ps.setObject(15, order.getOrderStartDate());
        ps.setObject(16, order.getOrderEndDate());
        setNullableInteger(ps, 17, order.getExecutionHours());
        setNullableInteger(ps, 18, order.getTotalDays());
        setNullableInteger(ps, 19, order.getQuantity());
        ps.setBigDecimal(20, order.getUnitPrice());
        ps.setBigDecimal(21, order.getTotalAmount());
        ps.setBigDecimal(22, order.getBalanceBefore());
        ps.setBigDecimal(23, order.getBalanceAfter());
        setNullableLong(ps, 24, order.getDeductedTransactionId());
        setNullableLong(ps, 25, order.getRefundTransactionId());
        ps.setString(26, order.getRejectReason());
        setNullableLong(ps, 27, order.getConfirmedByAdminId());
        ps.setObject(28, order.getConfirmedAt());
        setNullableLong(ps, 29, order.getExecutedByAdminId());
        ps.setObject(30, order.getExecutedAt());
        ps.setObject(31, order.getExpectedCompletedAt());
        ps.setObject(32, order.getCompletedAt());
        ps.setObject(33, order.getScheduledStartAt());
    }

    private static Integer readInteger(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static Long readLong(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDateTime readDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private static void setNullableLong(PreparedStatement ps, int index, Long value) throws java.sql.SQLException {
        if (value == null) {
            ps.setObject(index, null);
        } else {
            ps.setLong(index, value);
        }
    }

    private static void setNullableInteger(PreparedStatement ps, int index, Integer value) throws java.sql.SQLException {
        if (value == null) {
            ps.setObject(index, null);
        } else {
            ps.setInt(index, value);
        }
    }

    private record QueryParts(String sql, List<Object> params) {
    }
}
