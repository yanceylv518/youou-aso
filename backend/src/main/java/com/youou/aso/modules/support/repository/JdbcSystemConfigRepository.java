package com.youou.aso.modules.support.repository;

import com.youou.aso.modules.support.domain.SystemConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcSystemConfigRepository implements SystemConfigRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<SystemConfig> rowMapper = (rs, rowNum) -> {
        SystemConfig config = new SystemConfig();
        config.setKey(rs.getString("config_key"));
        config.setValue(rs.getString("config_value"));
        config.setSecret(rs.getBoolean("secret"));
        config.setDescription(rs.getString("description"));
        config.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return config;
    };

    public JdbcSystemConfigRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<SystemConfig> findByKey(String key) {
        return jdbcTemplate.query(
                "SELECT * FROM system_config WHERE config_key = ?",
                rowMapper,
                key
        ).stream().findFirst();
    }

    @Override
    public List<SystemConfig> findByKeys(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", keys.stream().map(key -> "?").toList());
        return jdbcTemplate.query(
                "SELECT * FROM system_config WHERE config_key IN (" + placeholders + ")",
                rowMapper,
                keys.toArray()
        );
    }

    @Override
    public SystemConfig save(String key, String value, boolean secret, String description) {
        jdbcTemplate.update(
                """
                        INSERT INTO system_config (config_key, config_value, secret, description)
                        VALUES (?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                            config_value = VALUES(config_value),
                            secret = VALUES(secret),
                            description = VALUES(description),
                            updated_at = CURRENT_TIMESTAMP
                        """,
                key,
                value,
                secret,
                description
        );
        return findByKey(key).orElseThrow();
    }
}
