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
        config.setEmail(rs.getString("email"));
        config.setEmailVisible(rs.getBoolean("email_visible"));
        config.setPhone(rs.getString("phone"));
        config.setPhoneVisible(rs.getBoolean("phone_visible"));
        config.setTeamsUrl(rs.getString("teams_url"));
        config.setTelegramUrl(rs.getString("telegram_url"));
        config.setTelegramQrUrl(rs.getString("telegram_qr_url"));
        config.setTelegramQrVisible(rs.getBoolean("telegram_qr_visible"));
        config.setWechatQrUrl(rs.getString("wechat_qr_url"));
        config.setWechatQrVisible(rs.getBoolean("wechat_qr_visible"));
        config.setWhatsappUrl(rs.getString("whatsapp_url"));
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
                        INSERT INTO customer_service_config (
                            id, service_name, qr_code_url, contact_hint,
                            email, email_visible, phone, phone_visible,
                            teams_url, telegram_url, telegram_qr_url, telegram_qr_visible,
                            wechat_qr_url, wechat_qr_visible, whatsapp_url, enabled
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                            service_name = VALUES(service_name),
                            qr_code_url = VALUES(qr_code_url),
                            contact_hint = VALUES(contact_hint),
                            email = VALUES(email),
                            email_visible = VALUES(email_visible),
                            phone = VALUES(phone),
                            phone_visible = VALUES(phone_visible),
                            teams_url = VALUES(teams_url),
                            telegram_url = VALUES(telegram_url),
                            telegram_qr_url = VALUES(telegram_qr_url),
                            telegram_qr_visible = VALUES(telegram_qr_visible),
                            wechat_qr_url = VALUES(wechat_qr_url),
                            wechat_qr_visible = VALUES(wechat_qr_visible),
                            whatsapp_url = VALUES(whatsapp_url),
                            enabled = VALUES(enabled),
                            updated_at = CURRENT_TIMESTAMP
                        """,
                CONFIG_ID,
                config.getServiceName(),
                config.getQrCodeUrl(),
                config.getContactHint(),
                config.getEmail(),
                config.isEmailVisible(),
                config.getPhone(),
                config.isPhoneVisible(),
                config.getTeamsUrl(),
                config.getTelegramUrl(),
                config.getTelegramQrUrl(),
                config.isTelegramQrVisible(),
                config.getWechatQrUrl(),
                config.isWechatQrVisible(),
                config.getWhatsappUrl(),
                config.isEnabled()
        );
        return find().orElse(config);
    }
}
