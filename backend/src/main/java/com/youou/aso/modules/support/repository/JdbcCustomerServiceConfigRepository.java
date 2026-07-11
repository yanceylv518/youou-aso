package com.youou.aso.modules.support.repository;

import com.youou.aso.modules.support.domain.CustomerServiceConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcCustomerServiceConfigRepository implements CustomerServiceConfigRepository {
    private static final long CONFIG_ID = 1L;

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<CustomerServiceConfig> rowMapper = (rs, rowNum) -> {
        CustomerServiceConfig config = new CustomerServiceConfig();
        config.setId(rs.getLong("id"));
        config.setServiceName(rs.getString("service_name"));
        config.setQrCodeUrl(rs.getString("qr_code_url"));
        config.setContactHint(rs.getString("contact_hint"));
        config.setEnabled(rs.getBoolean("enabled"));
        config.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return config;
    };

    public JdbcCustomerServiceConfigRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<CustomerServiceConfig> find() {
        return jdbcTemplate.query(
                "SELECT * FROM customer_service_config WHERE id = ?",
                rowMapper,
                CONFIG_ID
        ).stream().findFirst();
    }

    @Override
    public CustomerServiceConfig save(CustomerServiceConfig config) {
        jdbcTemplate.update(
                """
                        INSERT INTO customer_service_config (id, service_name, qr_code_url, contact_hint, enabled)
                        VALUES (?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                            service_name = VALUES(service_name),
                            qr_code_url = VALUES(qr_code_url),
                            contact_hint = VALUES(contact_hint),
                            enabled = VALUES(enabled),
                            updated_at = CURRENT_TIMESTAMP
                        """,
                CONFIG_ID,
                config.getServiceName(),
                config.getQrCodeUrl(),
                config.getContactHint(),
                config.isEnabled()
        );
        return find().orElse(config);
    }
}
