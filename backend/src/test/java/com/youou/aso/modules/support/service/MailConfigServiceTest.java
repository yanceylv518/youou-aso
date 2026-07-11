package com.youou.aso.modules.support.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.support.domain.SystemConfig;
import com.youou.aso.modules.support.dto.MailConfigResult;
import com.youou.aso.modules.support.dto.UpdateMailConfigCommand;
import com.youou.aso.modules.support.repository.SystemConfigRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MailConfigServiceTest {
    private final InMemorySystemConfigRepository repository = new InMemorySystemConfigRepository();
    private final MailConfigService service = new MailConfigService(
            new SystemConfigService(repository),
            "smtp.env.test",
            587,
            "env@example.com",
            "env-password",
            "sender@example.com",
            true,
            true,
            false
    );

    @Test
    void returnsFallbackSmtpWhenDatabaseConfigIsBlank() {
        MailConfigResult result = service.getConfig();

        assertThat(result.smtpHost()).isEqualTo("smtp.env.test");
        assertThat(result.username()).isEqualTo("env@example.com");
        assertThat(result.fromAddress()).isEqualTo("sender@example.com");
        assertThat(result.passwordConfigured()).isTrue();
    }

    @Test
    void updatesMailAndOrderNotificationConfig() {
        MailConfigResult result = service.updateConfig(new UpdateMailConfigCommand(
                "smtp.db.test",
                465,
                "db@example.com",
                "db-password",
                false,
                "from@example.com",
                true,
                false,
                true,
                "ops@example.com; admin@example.com\nops@example.com",
                true
        ));

        assertThat(result.smtpHost()).isEqualTo("smtp.db.test");
        assertThat(result.smtpPort()).isEqualTo(465);
        assertThat(result.username()).isEqualTo("db@example.com");
        assertThat(result.fromAddress()).isEqualTo("from@example.com");
        assertThat(result.sslEnabled()).isTrue();
        assertThat(result.orderNotificationRecipients()).isEqualTo("ops@example.com,admin@example.com");
        assertThat(service.getEnabledOrderNotificationRecipients()).containsExactly("ops@example.com", "admin@example.com");
    }

    @Test
    void keepsExistingPasswordWhenRequested() {
        service.updateConfig(new UpdateMailConfigCommand(
                "smtp.db.test",
                587,
                "db@example.com",
                "old-password",
                false,
                "from@example.com",
                true,
                true,
                false,
                null,
                false
        ));

        service.updateConfig(new UpdateMailConfigCommand(
                "smtp.db.test",
                587,
                "db@example.com",
                null,
                true,
                "from@example.com",
                true,
                true,
                false,
                null,
                false
        ));

        assertThat(repository.findByKey(MailConfigService.KEY_PASSWORD).orElseThrow().getValue()).isEqualTo("old-password");
    }

    @Test
    void rejectsEnabledOrderNotificationWithoutRecipients() {
        assertThatThrownBy(() -> service.updateConfig(new UpdateMailConfigCommand(
                "smtp.db.test",
                587,
                "db@example.com",
                "password",
                false,
                "from@example.com",
                true,
                true,
                false,
                "",
                true
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFIG_VALUE_INVALID);
    }

    @Test
    void rejectsInvalidEmail() {
        assertThatThrownBy(() -> service.updateConfig(new UpdateMailConfigCommand(
                "smtp.db.test",
                587,
                "db@example.com",
                "password",
                false,
                "not-an-email",
                true,
                true,
                false,
                null,
                false
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFIG_VALUE_INVALID);
    }

    private static final class InMemorySystemConfigRepository implements SystemConfigRepository {
        private final Map<String, SystemConfig> configs = new LinkedHashMap<>();

        @Override
        public Optional<SystemConfig> findByKey(String key) {
            return Optional.ofNullable(configs.get(key));
        }

        @Override
        public List<SystemConfig> findByKeys(Collection<String> keys) {
            return keys.stream()
                    .map(configs::get)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }

        @Override
        public SystemConfig save(String key, String value, boolean secret, String description) {
            SystemConfig config = new SystemConfig();
            config.setKey(key);
            config.setValue(value);
            config.setSecret(secret);
            config.setDescription(description);
            config.setUpdatedAt(LocalDateTime.of(2026, 7, 3, 13, 0));
            configs.put(key, config);
            return config;
        }
    }
}
