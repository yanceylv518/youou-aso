package com.youou.aso.modules.support.api;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.support.domain.CustomerServiceConfig;
import com.youou.aso.modules.support.dto.CustomerServiceConfigResult;
import com.youou.aso.modules.support.repository.CustomerServiceConfigRepository;
import com.youou.aso.modules.support.service.CustomerServiceConfigService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerServiceConfigControllerTest {
    private final CustomerServiceConfigController controller = new CustomerServiceConfigController(
            new CustomerServiceConfigService(new InMemoryCustomerServiceConfigRepository())
    );

    @Test
    void customerCanReadCustomerServiceConfig() {
        CustomerServiceConfigResult result = controller.getCustomerConfig(
                new AuthenticatedAccount(10L, AccountType.CUSTOMER.name(), "CUSTOMER")
        ).data();

        assertThat(result.enabled()).isTrue();
        assertThat(result.qrCodeUrl()).isEqualTo("https://static.example.test/qr.png");
    }

    @Test
    void customerCannotUpdateAdminCustomerServiceConfig() {
        assertThatThrownBy(() -> controller.updateAdminConfig(
                new AuthenticatedAccount(10L, AccountType.CUSTOMER.name(), "CUSTOMER"),
                new CustomerServiceConfigController.CustomerServiceConfigRequest(
                        "Support",
                        "https://static.example.test/qr.png",
                        "Scan to recharge",
                        "support@example.test",
                        null,
                        null,
                        null,
                        true
                )
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void adminCanUpdateCustomerServiceConfig() {
        CustomerServiceConfigResult result = controller.updateAdminConfig(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "SUPER_ADMIN"),
                new CustomerServiceConfigController.CustomerServiceConfigRequest(
                        "Support",
                        "https://static.example.test/new-qr.png",
                        "Scan to recharge",
                        "support@example.test",
                        "https://teams.example.test/support",
                        "https://t.me/example_support",
                        "https://wa.me/12025550123",
                        true
                )
        ).data();

        assertThat(result.serviceName()).isEqualTo("Support");
        assertThat(result.qrCodeUrl()).isEqualTo("https://static.example.test/new-qr.png");
        assertThat(result.email()).isEqualTo("support@example.test");
        assertThat(result.telegramUrl()).isEqualTo("https://t.me/example_support");
        assertThat(result.enabled()).isTrue();
    }

    private static final class InMemoryCustomerServiceConfigRepository implements CustomerServiceConfigRepository {
        private CustomerServiceConfig config;

        private InMemoryCustomerServiceConfigRepository() {
            config = new CustomerServiceConfig();
            config.setId(1L);
            config.setServiceName("Support");
            config.setQrCodeUrl("https://static.example.test/qr.png");
            config.setContactHint("Scan to recharge");
            config.setEmail("support@example.test");
            config.setEnabled(true);
            config.setUpdatedAt(LocalDateTime.of(2026, 6, 22, 12, 0));
        }

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
            config.setTeamsUrl(nextConfig.getTeamsUrl());
            config.setTelegramUrl(nextConfig.getTelegramUrl());
            config.setWhatsappUrl(nextConfig.getWhatsappUrl());
            config.setEnabled(nextConfig.isEnabled());
            config.setUpdatedAt(LocalDateTime.of(2026, 6, 22, 12, 0));
            return config;
        }
    }
}
