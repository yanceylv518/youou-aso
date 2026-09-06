package com.youou.aso.modules.support.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.support.domain.CustomerServiceConfig;
import com.youou.aso.modules.support.dto.CustomerServiceConfigResult;
import com.youou.aso.modules.support.dto.UpdateCustomerServiceConfigCommand;
import com.youou.aso.modules.support.repository.CustomerServiceConfigRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerServiceConfigServiceTest {
    private final InMemoryCustomerServiceConfigRepository repository = new InMemoryCustomerServiceConfigRepository();
    private final CustomerServiceConfigService service = new CustomerServiceConfigService(repository);

    @Test
    void returnsDefaultDisabledConfigWhenNotConfigured() {
        CustomerServiceConfigResult result = service.getConfig();

        assertThat(result.serviceName()).isEqualTo("Youou-ASO Support");
        assertThat(result.qrCodeUrl()).isNull();
        assertThat(result.enabled()).isFalse();
    }

    @Test
    void updatesEnabledCustomerServiceConfig() {
        CustomerServiceConfigResult result = service.updateConfig(new UpdateCustomerServiceConfigCommand(
                "Recharge Support",
                "https://static.example.test/qr.png",
                "Scan to contact support",
                "support@example.test",
                "https://teams.example.test/support",
                "https://t.me/example_support",
                "https://wa.me/12025550123",
                true
        ));

        assertThat(result.serviceName()).isEqualTo("Recharge Support");
        assertThat(result.qrCodeUrl()).isEqualTo("https://static.example.test/qr.png");
        assertThat(result.contactHint()).isEqualTo("Scan to contact support");
        assertThat(result.email()).isEqualTo("support@example.test");
        assertThat(result.whatsappUrl()).isEqualTo("https://wa.me/12025550123");
        assertThat(result.enabled()).isTrue();
    }

    @Test
    void rejectsVisibleChannelWithoutConfiguredValue() {
        assertThatThrownBy(() -> service.updateConfig(new UpdateCustomerServiceConfigCommand(
                "Recharge Support",
                null,
                "Scan to contact support",
                "",
                true,
                null,
                false,
                null,
                null,
                null,
                false,
                null,
                false,
                null,
                true
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFIG_VALUE_INVALID);
    }

    @Test
    void acceptsEnabledConfigWithEmailOnly() {
        CustomerServiceConfigResult result = service.updateConfig(new UpdateCustomerServiceConfigCommand(
                "Customer Support",
                null,
                "Contact support",
                "support@example.test",
                null,
                null,
                null,
                true
        ));

        assertThat(result.enabled()).isTrue();
        assertThat(result.qrCodeUrl()).isNull();
        assertThat(result.email()).isEqualTo("support@example.test");
    }

    @Test
    void rejectsNonHttpQrCodeUrl() {
        assertThatThrownBy(() -> service.updateConfig(new UpdateCustomerServiceConfigCommand(
                "Recharge Support",
                "javascript:alert(1)",
                "Scan to contact support",
                null,
                null,
                null,
                null,
                false
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFIG_VALUE_INVALID);
    }

    private static final class InMemoryCustomerServiceConfigRepository implements CustomerServiceConfigRepository {
        private CustomerServiceConfig config;

        @Override
        public Optional<CustomerServiceConfig> find() {
            return Optional.ofNullable(config);
        }

        @Override
        public CustomerServiceConfig save(CustomerServiceConfig nextConfig) {
            config = new CustomerServiceConfig();
            config.setId(1L);
            config.setServiceName(nextConfig.getServiceName());
            config.setQrCodeUrl(nextConfig.getQrCodeUrl());
            config.setContactHint(nextConfig.getContactHint());
            config.setEmail(nextConfig.getEmail());
            config.setEmailVisible(nextConfig.isEmailVisible());
            config.setPhone(nextConfig.getPhone());
            config.setPhoneVisible(nextConfig.isPhoneVisible());
            config.setTeamsUrl(nextConfig.getTeamsUrl());
            config.setTelegramUrl(nextConfig.getTelegramUrl());
            config.setTelegramQrUrl(nextConfig.getTelegramQrUrl());
            config.setTelegramQrVisible(nextConfig.isTelegramQrVisible());
            config.setWechatQrUrl(nextConfig.getWechatQrUrl());
            config.setWechatQrVisible(nextConfig.isWechatQrVisible());
            config.setWhatsappUrl(nextConfig.getWhatsappUrl());
            config.setEnabled(nextConfig.isEnabled());
            config.setUpdatedAt(LocalDateTime.of(2026, 6, 22, 12, 0));
            return config;
        }
    }
}
