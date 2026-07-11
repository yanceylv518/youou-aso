package com.youou.aso.modules.account.repository;

import com.youou.aso.modules.account.domain.AccountStatus;
import com.youou.aso.modules.account.domain.AdminAccount;
import com.youou.aso.modules.account.domain.AdminRole;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcAdminAccountRepository implements AdminAccountRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<AdminAccount> rowMapper = (rs, rowNum) -> {
        AdminAccount admin = new AdminAccount();
        admin.setId(rs.getLong("id"));
        admin.setUsername(rs.getString("username"));
        admin.setEmail(rs.getString("email"));
        admin.setPasswordHash(rs.getString("password_hash"));
        admin.setRoleCode(AdminRole.valueOf(rs.getString("role_code")));
        admin.setStatus(AccountStatus.valueOf(rs.getString("status")));
        admin.setForcePasswordChange(rs.getBoolean("force_password_change"));
        admin.setPreferredLocale(rs.getString("preferred_locale"));
        var lastLoginAt = rs.getTimestamp("last_login_at");
        admin.setLastLoginAt(lastLoginAt == null ? null : lastLoginAt.toLocalDateTime());
        admin.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        admin.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return admin;
    };

    public JdbcAdminAccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admin_account WHERE username = ?",
                Integer.class,
                username
        );
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admin_account WHERE email = ?",
                Integer.class,
                email
        );
        return count != null && count > 0;
    }

    @Override
    public Optional<AdminAccount> findById(Long id) {
        return jdbcTemplate.query("SELECT * FROM admin_account WHERE id = ?", rowMapper, id).stream().findFirst();
    }

    @Override
    public Optional<AdminAccount> findByUsernameOrEmail(String account) {
        return jdbcTemplate.query(
                "SELECT * FROM admin_account WHERE username = ? OR email = ?",
                rowMapper,
                account,
                account
        ).stream().findFirst();
    }

    @Override
    public List<AdminAccount> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM admin_account ORDER BY created_at DESC, id DESC",
                rowMapper
        );
    }

    @Override
    public AdminAccount save(AdminAccount admin) {
        jdbcTemplate.update(
                """
                        INSERT INTO admin_account (
                            username, email, password_hash, role_code, status,
                            force_password_change, preferred_locale
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                admin.getUsername(),
                admin.getEmail(),
                admin.getPasswordHash(),
                admin.getRoleCode().name(),
                admin.getStatus().name(),
                admin.isForcePasswordChange(),
                admin.getPreferredLocale()
        );
        return findByUsernameOrEmail(admin.getUsername()).orElseThrow();
    }

    @Override
    public void updateStatus(Long id, AccountStatus status) {
        jdbcTemplate.update(
                "UPDATE admin_account SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                status.name(),
                id
        );
    }

    @Override
    public void updatePassword(Long id, String passwordHash, boolean forcePasswordChange) {
        jdbcTemplate.update(
                """
                        UPDATE admin_account
                        SET password_hash = ?, force_password_change = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                passwordHash,
                forcePasswordChange,
                id
        );
    }
}
