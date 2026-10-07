package com.youou.aso.modules.order.repository;

import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.order.domain.SpecialAuditStatus;
import com.youou.aso.modules.order.domain.SpecialOrderAudit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcSpecialOrderAuditRepository implements SpecialOrderAuditRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<SpecialOrderAudit> rowMapper = (rs, rowNum) -> {
        SpecialOrderAudit audit = new SpecialOrderAudit();
        audit.setId(rs.getLong("id"));
        audit.setOrderModuleId(readLong(rs,"order_module_id"));
        audit.setAuditNo(rs.getString("audit_no"));
        audit.setCustomerId(rs.getLong("customer_id"));
        audit.setCustomerAppId(rs.getLong("customer_app_id"));
        audit.setOrderType(OrderType.valueOf(rs.getString("order_type")));
        audit.setStoreType(StoreType.valueOf(rs.getString("store_type")));
        audit.setRegionCode(rs.getString("region_code"));
        audit.setAppIdentifier(rs.getString("app_identifier"));
        audit.setAppName(rs.getString("app_name"));
        audit.setAppIconUrl(rs.getString("app_icon_url"));
        audit.setRequestedContent(rs.getString("requested_content"));
        audit.setContactType(rs.getString("contact_type"));
        audit.setContactValue(rs.getString("contact_value"));
        audit.setNegotiatedContent(rs.getString("negotiated_content"));
        audit.setNegotiatedPrice(rs.getBigDecimal("negotiated_price"));
        audit.setStatus(SpecialAuditStatus.valueOf(rs.getString("status")));
        audit.setReviewedByAdminId(readLong(rs, "reviewed_by_admin_id"));
        audit.setReviewedAt(readDateTime(rs.getTimestamp("reviewed_at")));
        audit.setCancelReason(rs.getString("cancel_reason"));
        audit.setSubmittedOrderId(readLong(rs, "submitted_order_id"));
        audit.setSubmittedAt(readDateTime(rs.getTimestamp("submitted_at")));
        audit.setCreatedAt(readDateTime(rs.getTimestamp("created_at")));
        audit.setUpdatedAt(readDateTime(rs.getTimestamp("updated_at")));
        return audit;
    };

    public JdbcSpecialOrderAuditRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public SpecialOrderAudit save(SpecialOrderAudit audit) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                            INSERT INTO special_order_audit
                            (audit_no, customer_id, customer_app_id, order_type, store_type, region_code,
                             app_identifier, app_name, app_icon_url, requested_content, contact_type, contact_value, negotiated_content,
                             negotiated_price, status, reviewed_by_admin_id, reviewed_at, cancel_reason,
                             submitted_order_id, submitted_at, order_module_id)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS
            );
            bind(ps, audit);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            audit.setId(key.longValue());
        }
        return findById(audit.getId()).orElse(audit);
    }

    @Override
    public Optional<SpecialOrderAudit> findById(Long id) {
        return jdbcTemplate.query("SELECT * FROM special_order_audit WHERE id = ?", rowMapper, id).stream().findFirst();
    }

    @Override
    public Optional<SpecialOrderAudit> findByIdForUpdate(Long id) {
        return jdbcTemplate.query("SELECT * FROM special_order_audit WHERE id = ? FOR UPDATE", rowMapper, id).stream().findFirst();
    }

    @Override
    public SpecialOrderAudit update(SpecialOrderAudit audit) {
        jdbcTemplate.update(
                """
                        UPDATE special_order_audit
                        SET region_code = ?, negotiated_content = ?, negotiated_price = ?, status = ?, reviewed_by_admin_id = ?,
                            reviewed_at = ?, cancel_reason = ?, submitted_order_id = ?, submitted_at = ?,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                audit.getRegionCode(),
                audit.getNegotiatedContent(),
                audit.getNegotiatedPrice(),
                audit.getStatus().name(),
                audit.getReviewedByAdminId(),
                audit.getReviewedAt(),
                audit.getCancelReason(),
                audit.getSubmittedOrderId(),
                audit.getSubmittedAt(),
                audit.getId()
        );
        return findById(audit.getId()).orElse(audit);
    }

    @Override
    public List<SpecialOrderAudit> findByCustomerId(Long customerId) {
        return jdbcTemplate.query(
                "SELECT * FROM special_order_audit WHERE customer_id = ? ORDER BY created_at DESC, id DESC",
                rowMapper,
                customerId
        );
    }

    @Override
    public List<SpecialOrderAudit> findByCustomerId(Long customerId, int limit, int offset) {
        return jdbcTemplate.query(
                "SELECT * FROM special_order_audit WHERE customer_id = ? ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?",
                rowMapper,
                customerId,
                limit,
                offset
        );
    }

    @Override
    public long countByCustomerId(Long customerId) {
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM special_order_audit WHERE customer_id = ?",
                Long.class,
                customerId
        );
        return total == null ? 0L : total;
    }

    @Override
    public List<SpecialOrderAudit> findAll() {
        return jdbcTemplate.query("SELECT * FROM special_order_audit ORDER BY created_at DESC, id DESC", rowMapper);
    }

    @Override
    public List<SpecialOrderAudit> findAll(int limit, int offset) {
        return jdbcTemplate.query(
                "SELECT * FROM special_order_audit ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?",
                rowMapper,
                limit,
                offset
        );
    }

    @Override
    public long countAll() {
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM special_order_audit", Long.class);
        return total == null ? 0L : total;
    }

    private void bind(PreparedStatement ps, SpecialOrderAudit audit) throws java.sql.SQLException {
        ps.setObject(21,audit.getOrderModuleId());
        ps.setString(1, audit.getAuditNo());
        ps.setLong(2, audit.getCustomerId());
        ps.setLong(3, audit.getCustomerAppId());
        ps.setString(4, audit.getOrderType().name());
        ps.setString(5, audit.getStoreType().name());
        ps.setString(6, audit.getRegionCode());
        ps.setString(7, audit.getAppIdentifier());
        ps.setString(8, audit.getAppName());
        ps.setString(9, audit.getAppIconUrl());
        ps.setString(10, audit.getRequestedContent());
        ps.setString(11, audit.getContactType());
        ps.setString(12, audit.getContactValue());
        ps.setString(13, audit.getNegotiatedContent());
        ps.setBigDecimal(14, audit.getNegotiatedPrice());
        ps.setString(15, audit.getStatus().name());
        setNullableLong(ps, 16, audit.getReviewedByAdminId());
        ps.setObject(17, audit.getReviewedAt());
        ps.setString(18, audit.getCancelReason());
        setNullableLong(ps, 19, audit.getSubmittedOrderId());
        ps.setObject(20, audit.getSubmittedAt());
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
}
