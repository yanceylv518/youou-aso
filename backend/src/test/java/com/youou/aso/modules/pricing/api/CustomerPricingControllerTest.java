package com.youou.aso.modules.pricing.api;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.pricing.domain.PriceCode;
import com.youou.aso.modules.pricing.domain.PricingConfig;
import com.youou.aso.modules.pricing.dto.PricingConfigResult;
import com.youou.aso.modules.pricing.repository.PricingConfigRepository;
import com.youou.aso.modules.pricing.service.PricingService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerPricingControllerTest {
    private final CustomerPricingController controller = new CustomerPricingController(
            new PricingService(new InMemoryPricingConfigRepository())
    );

    @Test
    void customerCanReadPricingForOrderCreation() {
        List<PricingConfigResult> result = controller.listPricing(
                new AuthenticatedAccount(10L, AccountType.CUSTOMER.name(), "CUSTOMER")
        ).data();

        assertThat(result).hasSize(6);
        assertThat(result)
                .extracting(PricingConfigResult::code)
                .containsExactlyInAnyOrder(
                        PriceCode.KEYWORD_INSTALL,
                        PriceCode.DOWNLOAD,
                        PriceCode.RATING_5,
                        PriceCode.RATING_4,
                        PriceCode.REVIEW_5,
                        PriceCode.REVIEW_4
                );
    }

    @Test
    void adminCannotReadCustomerPricingEndpoint() {
        assertThatThrownBy(() -> controller.listPricing(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "SUPER_ADMIN")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    private static final class InMemoryPricingConfigRepository implements PricingConfigRepository {
        private final Map<PriceCode, PricingConfig> prices = new EnumMap<>(PriceCode.class);

        private InMemoryPricingConfigRepository() {
            for (PriceCode code : PriceCode.values()) {
                prices.put(code, PricingConfig.enabled(code, BigDecimal.ONE));
            }
        }

        @Override
        public List<PricingConfig> findAll() {
            return List.copyOf(prices.values());
        }

        @Override
        public Optional<PricingConfig> findByCode(PriceCode code) {
            return Optional.ofNullable(prices.get(code));
        }

        @Override
        public void saveAll(List<PricingConfig> configs) {
            configs.forEach(config -> prices.put(config.getCode(), config));
        }
    }
}
