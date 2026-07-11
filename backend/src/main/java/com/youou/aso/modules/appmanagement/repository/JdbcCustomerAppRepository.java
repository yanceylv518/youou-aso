package com.youou.aso.modules.appmanagement.repository;

import com.youou.aso.modules.appmanagement.domain.CustomerApp;
import com.youou.aso.modules.appmanagement.domain.CustomerAppStatus;
import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.appmanagement.dto.CustomerAppQuery;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcCustomerAppRepository implements CustomerAppRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<CustomerApp> rowMapper = (rs, rowNum) -> {
        CustomerApp app = new CustomerApp();
        app.setId(rs.getLong("id"));
        app.setCustomerId(rs.getLong("customer_id"));
        app.setStoreType(StoreType.valueOf(rs.getString("store_type")));
        app.setRegionCode(rs.getString("region_code"));
        app.setAppIdentifier(rs.getString("app_identifier"));
        app.setAppName(rs.getString("app_name"));
        app.setAppIconUrl(rs.getString("app_icon_url"));
        app.setBundleId(rs.getString("bundle_id"));
        app.setExternalAppId(rs.getString("external_app_id"));
        app.setCategory(rs.getString("category"));
        try {
            app.setCustomerUsername(rs.getString("customer_username"));
            app.setCustomerEmail(rs.getString("customer_email"));
        } catch (java.sql.SQLException ignored) {
            // 普通用户列表不查询客户信息列，管理员列表会通过 JOIN 补齐。
        }
        app.setStatus(CustomerAppStatus.valueOf(rs.getString("status")));
        app.setVerifiedAt(rs.getTimestamp("verified_at").toLocalDateTime());
        app.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        app.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return app;
    };

    public JdbcCustomerAppRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<CustomerApp> findById(Long id) {
        return jdbcTemplate.query("SELECT * FROM customer_app WHERE id = ?", rowMapper, id).stream().findFirst();
    }

    @Override
    public Optional<CustomerApp> findActiveByCustomerStoreAndIdentifier(Long customerId, StoreType storeType, String appIdentifier) {
        return jdbcTemplate.query(
                """
                        SELECT *
                        FROM customer_app
                        WHERE customer_id = ? AND store_type = ? AND app_identifier = ?
                          AND status = ?
                        ORDER BY id ASC
                        LIMIT 1
                        """,
                rowMapper,
                customerId,
                storeType.name(),
                appIdentifier,
                CustomerAppStatus.ACTIVE.name()
        ).stream().findFirst();
    }

    @Override
    public boolean existsByCustomerAndStoreAndRegionAndIdentifier(Long customerId, StoreType storeType, String regionCode, String appIdentifier) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM customer_app
                        WHERE customer_id = ? AND store_type = ? AND region_code = ? AND app_identifier = ?
                          AND status = ?
                        """,
                Integer.class,
                customerId,
                storeType.name(),
                regionCode,
                appIdentifier,
                CustomerAppStatus.ACTIVE.name()
        );
        return count != null && count > 0;
    }

    @Override
    public boolean existsByCustomerAndStoreAndRegionAndIdentifierExcludingId(Long customerId, StoreType storeType, String regionCode, String appIdentifier, Long excludedId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM customer_app
                        WHERE customer_id = ? AND store_type = ? AND region_code = ? AND app_identifier = ?
                          AND status = ? AND id <> ?
                        """,
                Integer.class,
                customerId,
                storeType.name(),
                regionCode,
                appIdentifier,
                CustomerAppStatus.ACTIVE.name(),
                excludedId
        );
        return count != null && count > 0;
    }

    @Override
    public CustomerApp save(CustomerApp app) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                            INSERT INTO customer_app
                            (customer_id, store_type, region_code, app_identifier, app_name, app_icon_url,
                             bundle_id, external_app_id, category, status, verified_at)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, app.getCustomerId());
            ps.setString(2, app.getStoreType().name());
            ps.setString(3, app.getRegionCode());
            ps.setString(4, app.getAppIdentifier());
            ps.setString(5, app.getAppName());
            ps.setString(6, app.getAppIconUrl());
            ps.setString(7, app.getBundleId());
            ps.setString(8, app.getExternalAppId());
            ps.setString(9, app.getCategory());
            ps.setString(10, app.getStatus().name());
            ps.setObject(11, app.getVerifiedAt());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            app.setId(key.longValue());
        }
        addRegion(app.getId(), app.getRegionCode());
        return findById(app.getId()).orElse(app);
    }

    @Override
    public void addRegion(Long customerAppId, String regionCode) {
        jdbcTemplate.update(
                """
                        INSERT IGNORE INTO customer_app_region (customer_app_id, region_code)
                        VALUES (?, ?)
                        """,
                customerAppId,
                regionCode
        );
    }

    @Override
    public boolean existsRegion(Long customerAppId, String regionCode) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM customer_app_region
                        WHERE customer_app_id = ? AND region_code = ?
                        """,
                Integer.class,
                customerAppId,
                regionCode
        );
        return count != null && count > 0;
    }

    @Override
    public Map<Long, List<String>> findRegionCodesByAppIds(List<Long> appIds) {
        if (appIds == null || appIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = String.join(",", java.util.Collections.nCopies(appIds.size(), "?"));
        Map<Long, List<String>> result = new LinkedHashMap<>();
        jdbcTemplate.query(
                "SELECT customer_app_id, region_code FROM customer_app_region WHERE customer_app_id IN (" + placeholders + ") ORDER BY customer_app_id ASC, region_code ASC",
                rs -> {
                    long appId = rs.getLong("customer_app_id");
                    result.computeIfAbsent(appId, ignored -> new ArrayList<>()).add(rs.getString("region_code"));
                },
                appIds.toArray()
        );
        return result;
    }

    @Override
    public CustomerApp update(CustomerApp app) {
        jdbcTemplate.update(
                """
                        UPDATE customer_app
                        SET store_type = ?, region_code = ?, app_identifier = ?, app_name = ?, app_icon_url = ?,
                            bundle_id = ?, external_app_id = ?, category = ?, status = ?, verified_at = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                app.getStoreType().name(),
                app.getRegionCode(),
                app.getAppIdentifier(),
                app.getAppName(),
                app.getAppIconUrl(),
                app.getBundleId(),
                app.getExternalAppId(),
                app.getCategory(),
                app.getStatus().name(),
                app.getVerifiedAt(),
                app.getId()
        );
        return findById(app.getId()).orElse(app);
    }

    @Override
    public void updateStatus(Long id, CustomerAppStatus status) {
        jdbcTemplate.update(
                "UPDATE customer_app SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                status.name(),
                id
        );
    }

    @Override
    public List<CustomerApp> findByCustomerId(Long customerId, CustomerAppQuery query) {
        QueryParts parts = buildQuery("SELECT ca.* FROM customer_app ca WHERE ca.customer_id = ?", List.of(customerId), query);
        return jdbcTemplate.query(parts.sql(), rowMapper, parts.params().toArray());
    }

    @Override
    public List<CustomerApp> findByCustomerId(Long customerId, CustomerAppQuery query, int limit, int offset) {
        QueryParts parts = buildQuery("SELECT ca.* FROM customer_app ca WHERE ca.customer_id = ?", List.of(customerId), query);
        List<Object> params = new ArrayList<>(parts.params());
        params.add(limit);
        params.add(offset);
        return jdbcTemplate.query(parts.sql() + " LIMIT ? OFFSET ?", rowMapper, params.toArray());
    }

    @Override
    public long countByCustomerId(Long customerId, CustomerAppQuery query) {
        QueryParts parts = buildQuery("SELECT ca.* FROM customer_app ca WHERE ca.customer_id = ?", List.of(customerId), query);
        return count(parts);
    }

    @Override
    public List<CustomerApp> findAll(CustomerAppQuery query) {
        QueryParts parts = buildQuery(
                """
                        SELECT ca.*, c.username AS customer_username, c.email AS customer_email
                        FROM customer_app ca
                        JOIN customer_account c ON c.id = ca.customer_id
                        WHERE 1 = 1
                        """,
                List.of(),
                query
        );
        return jdbcTemplate.query(parts.sql(), rowMapper, parts.params().toArray());
    }

    @Override
    public List<CustomerApp> findAll(CustomerAppQuery query, int limit, int offset) {
        QueryParts parts = buildQuery(
                """
                        SELECT ca.*, c.username AS customer_username, c.email AS customer_email
                        FROM customer_app ca
                        JOIN customer_account c ON c.id = ca.customer_id
                        WHERE 1 = 1
                        """,
                List.of(),
                query
        );
        List<Object> params = new ArrayList<>(parts.params());
        params.add(limit);
        params.add(offset);
        return jdbcTemplate.query(parts.sql() + " LIMIT ? OFFSET ?", rowMapper, params.toArray());
    }

    @Override
    public long countAll(CustomerAppQuery query) {
        QueryParts parts = buildQuery(
                """
                        SELECT ca.*, c.username AS customer_username, c.email AS customer_email
                        FROM customer_app ca
                        JOIN customer_account c ON c.id = ca.customer_id
                        WHERE 1 = 1
                        """,
                List.of(),
                query
        );
        return count(parts);
    }

    private long count(QueryParts parts) {
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM (" + parts.sql() + ") counted",
                Long.class,
                parts.params().toArray()
        );
        return total == null ? 0L : total;
    }

    private QueryParts buildQuery(String baseSql, List<Object> baseParams, CustomerAppQuery query) {
        StringBuilder sql = new StringBuilder(baseSql);
        List<Object> params = new ArrayList<>(baseParams);
        CustomerAppStatus status = query != null && query.status() != null ? query.status() : CustomerAppStatus.ACTIVE;
        sql.append(" AND ca.status = ?");
        params.add(status.name());

        if (query != null && query.storeType() != null) {
            sql.append(" AND ca.store_type = ?");
            params.add(query.storeType().name());
        }
        if (query != null && query.regionCode() != null && !query.regionCode().isBlank()) {
            sql.append(" AND EXISTS (SELECT 1 FROM customer_app_region car WHERE car.customer_app_id = ca.id AND car.region_code = ?)");
            params.add(query.regionCode().trim().toUpperCase());
        }
        if (query != null && query.keyword() != null && !query.keyword().isBlank()) {
            String keyword = "%" + query.keyword().trim() + "%";
            sql.append(" AND (ca.app_name LIKE ? OR ca.app_identifier LIKE ? OR ca.bundle_id LIKE ? OR ca.external_app_id LIKE ? OR ca.category LIKE ?)");
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        sql.append(" ORDER BY ca.created_at DESC, ca.id DESC");
        return new QueryParts(sql.toString(), params);
    }

    private record QueryParts(String sql, List<Object> params) {
    }
}
