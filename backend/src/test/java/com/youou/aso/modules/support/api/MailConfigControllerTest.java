package com.youou.aso.modules.support.api;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.domain.AdminRole;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.support.domain.SystemConfig;
import com.youou.aso.modules.support.dto.MailConfigResult;
import com.youou.aso.modules.support.repository.SystemConfigRepository;
import com.youou.aso.modules.support.service.MailConfigService;
import com.youou.aso.modules.support.service.SystemConfigService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MailConfigControllerTest {
    private final MailConfigController controller = new MailConfigController(
            new MailConfigService(
                    new SystemConfigService(new InMemorySystemConfigRepository()),
                    "",
                    587,
                    "",
                    "",
                    "",
                    true,
                    true,
                    false
            )
    );

    @Test
    void superAdminCanReadMailConfig() {
        MailConfigResult result = controller.getConfig(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), AdminRole.SUPER_ADMIN.name())
        ).data();

        assertThat(result.smtpPort()).isEqualTo(587);
        assertThat(result.orderNotificationEnabled()).isFalse();
    }

    @Test
    void regularAdminCannotReadMailConfig() {
        assertThatThrownBy(() -> controller.getConfig(
                new AuthenticatedAccount(2L, AccountType.ADMIN.name(), AdminRole.ADMIN.name())
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void customerCannotUpdateMailConfig() {
        assertThatThrownBy(() -> controller.updateConfig(
                new AuthenticatedAccount(10L, AccountType.CUSTOMER.name(), "CUSTOMER"),
                new MailConfigController.MailConfigRequest(
                        "smtp.example.com",
                        587,
                        "sender@example.com",
                        "password",
                        false,
                        "sender@example.com",
                        true,
                        true,
                        false,
                        "ops@example.com",
                        true
                )
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void superAdminCanUpdateMailConfig() {
        MailConfigResult result = controller.updateConfig(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), AdminRole.SUPER_ADMIN.name()),
                new MailConfigController.MailConfigRequest(
                        "smtp.example.com",
                        465,
                        "sender@example.com",
                        "password",
                        false,
                        "sender@example.com",
                        true,
                        false,
                        true,
                        "ops@example.com;admin@example.com",
                        true
                )
        ).data();

        assertThat(result.smtpHost()).isEqualTo("smtp.example.com");
        assertThat(result.sslEnabled()).isTrue();
        assertThat(result.orderNotificationRecipients()).isEqualTo("ops@example.com,admin@example.com");
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
