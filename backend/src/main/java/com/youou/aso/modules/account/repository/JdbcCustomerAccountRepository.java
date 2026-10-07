package com.youou.aso.modules.account.repository;

import com.youou.aso.modules.account.domain.AccountStatus;
import com.youou.aso.modules.account.domain.CustomerAccount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JdbcCustomerAccountRepository implements CustomerAccountRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<CustomerAccount> rowMapper = (rs, rowNum) -> {
        CustomerAccount customer = new CustomerAccount();
        customer.setId(rs.getLong("id"));
        customer.setUsername(rs.getString("username"));
        customer.setEmail(rs.getString("email"));
        customer.setPasswordHash(rs.getString("password_hash"));
        customer.setStatus(AccountStatus.valueOf(rs.getString("status")));
        customer.setForcePasswordChange(rs.getBoolean("force_password_change"));
        customer.setPreferredLocale(rs.getString("preferred_locale"));
        var lastLoginAt = rs.getTimestamp("last_login_at");
        customer.setLastLoginAt(lastLoginAt == null ? null : lastLoginAt.toLocalDateTime());
        customer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        customer.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return customer;
    };

    public JdbcCustomerAccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void lockRegistration() {
        jdbcTemplate.queryForObject("SELECT id FROM account_registration_lock WHERE id = 1 FOR UPDATE", Integer.class);
    }

    @Override
    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer_account WHERE username = ?",
                Integer.class,
                username
        );
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer_account WHERE email = ?",
                Integer.class,
                email
        );
        return count != null && count > 0;
    }

    @Override
    public Optional<CustomerAccount> findById(Long id) {
        return jdbcTemplate.query("SELECT * FROM customer_account WHERE id = ?", rowMapper, id).stream().findFirst();
    }

    @Override
    public List<CustomerAccount> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Long> distinctIds = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (distinctIds.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", Collections.nCopies(distinctIds.size(), "?"));
        return jdbcTemplate.query(
                "SELECT * FROM customer_account WHERE id IN (" + placeholders + ")",
                rowMapper,
                distinctIds.toArray()
        );
    }

    @Override
    public Optional<CustomerAccount> findByUsernameOrEmail(String account) {
        return jdbcTemplate.query(
                "SELECT * FROM customer_account WHERE username = ? OR email = ?",
                rowMapper,
                account,
                account
        ).stream().findFirst();
    }

    @Override
    public List<CustomerAccount> findAll(String keyword) {
        QueryParts parts = buildQuery(keyword);
        return jdbcTemplate.query(parts.sql(), rowMapper, parts.params().toArray());
    }

    @Override
    public List<CustomerAccount> findAll(String keyword, int limit, int offset) {
        QueryParts parts = buildQuery(keyword);
        List<Object> params = new ArrayList<>(parts.params());
        params.add(limit);
        params.add(offset);
        return jdbcTemplate.query(parts.sql() + " LIMIT ? OFFSET ?", rowMapper, params.toArray());
    }

    @Override
    public long countAll(String keyword) {
        QueryParts parts = buildQuery(keyword);
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM (" + parts.sql() + ") counted",
                Long.class,
                parts.params().toArray()
        );
        return total == null ? 0L : total;
    }

    @Override
    public CustomerAccount save(CustomerAccount customer) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                            INSERT INTO customer_account
                            (username, email, password_hash, status, force_password_change, preferred_locale)
                            VALUES (?, ?, ?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, customer.getUsername());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPasswordHash());
            ps.setString(4, customer.getStatus().name());
            ps.setBoolean(5, customer.isForcePasswordChange());
            ps.setString(6, customer.getPreferredLocale());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            customer.setId(key.longValue());
        }
        return customer;
    }

    @Override
    public void updateStatus(Long id, AccountStatus status) {
        jdbcTemplate.update(
                "UPDATE customer_account SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                status.name(),
                id
        );
    }

    @Override
    public void updatePassword(Long id, String passwordHash, boolean forcePasswordChange) {
        jdbcTemplate.update(
                """
                        UPDATE customer_account
                        SET password_hash = ?, force_password_change = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                passwordHash,
                forcePasswordChange,
                id
        );
    }

    private QueryParts buildQuery(String keyword) {
        StringBuilder sql = new StringBuilder("SELECT * FROM customer_account");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" WHERE username LIKE ? OR email LIKE ?");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
        }
        sql.append(" ORDER BY created_at DESC, id DESC");
        return new QueryParts(sql.toString(), params);
    }

    private record QueryParts(String sql, List<Object> params) {
    }
}
