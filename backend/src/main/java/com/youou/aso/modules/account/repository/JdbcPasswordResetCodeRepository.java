package com.youou.aso.modules.account.repository;

import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.domain.PasswordResetCode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Repository
public class JdbcPasswordResetCodeRepository implements PasswordResetCodeRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<PasswordResetCode> rowMapper = (rs, rowNum) -> {
        PasswordResetCode code = new PasswordResetCode();
        code.setId(rs.getLong("id"));
        code.setEmail(rs.getString("email"));
        code.setAccountType(AccountType.valueOf(rs.getString("account_type")));
        code.setAccountId(rs.getLong("account_id"));
        code.setCodeHash(rs.getString("code_hash"));
        code.setAttemptCount(rs.getInt("attempt_count"));
        code.setExpiresAt(rs.getTimestamp("expires_at").toInstant());
        Timestamp usedAt = rs.getTimestamp("used_at");
        code.setUsedAt(usedAt == null ? null : usedAt.toInstant());
        code.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        return code;
    };

    public JdbcPasswordResetCodeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void invalidateActiveCodes(String email) {
        jdbcTemplate.update(
                """
                        UPDATE password_reset_code
                        SET used_at = CURRENT_TIMESTAMP
                        WHERE email = ? AND used_at IS NULL
                        """,
                email
        );
    }

    @Override
    public PasswordResetCode save(PasswordResetCode code) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                            INSERT INTO password_reset_code
                                (email, account_type, account_id, code_hash, attempt_count, expires_at, used_at, created_at)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, code.getEmail());
            ps.setString(2, code.getAccountType().name());
            ps.setLong(3, code.getAccountId());
            ps.setString(4, code.getCodeHash());
            ps.setInt(5, code.getAttemptCount());
            ps.setTimestamp(6, Timestamp.from(code.getExpiresAt()));
            ps.setTimestamp(7, code.getUsedAt() == null ? null : Timestamp.from(code.getUsedAt()));
            ps.setTimestamp(8, Timestamp.from(code.getCreatedAt()));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            code.setId(key.longValue());
        }
        return code;
    }

    @Override
    public Optional<PasswordResetCode> findLatestActiveByEmail(String email, Instant now) {
        return jdbcTemplate.query(
                """
                        SELECT *
                        FROM password_reset_code
                        WHERE email = ? AND used_at IS NULL AND expires_at > ?
                        ORDER BY created_at DESC, id DESC
                        LIMIT 1
                        """,
                rowMapper,
                email,
                Timestamp.from(now)
        ).stream().findFirst();
    }

    @Override
    public void incrementAttemptCount(Long id) {
        jdbcTemplate.update(
                """
                        UPDATE password_reset_code
                        SET attempt_count = attempt_count + 1, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                id
        );
    }

    @Override
    public void markUsed(Long id, Instant usedAt) {
        jdbcTemplate.update(
                """
                        UPDATE password_reset_code
                        SET used_at = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                Timestamp.from(usedAt),
                id
        );
    }
}
