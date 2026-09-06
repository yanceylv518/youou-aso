package com.youou.aso.modules.pricing.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.pricing.domain.PriceCode;
import com.youou.aso.modules.pricing.domain.PricingConfig;
import com.youou.aso.modules.pricing.dto.UpdatePricingCommand;
import com.youou.aso.modules.pricing.repository.PricingConfigRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricingServiceTest {
    private final InMemoryPricingConfigRepository repository = new InMemoryPricingConfigRepository();
    private final PricingService pricingService = new PricingService(repository);

    @Test
    void listPricingReturnsSixStandardPriceItems() {
        var result = pricingService.listPricing();

        assertThat(result).hasSize(6);
        assertThat(result)
                .extracting(PricingConfig::getCode)
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
    void updatePricingPersistsNonNegativePrices() {
        pricingService.updatePricing(List.of(
                new UpdatePricingCommand(PriceCode.KEYWORD_INSTALL, new BigDecimal("1.20"), new BigDecimal("1.80")),
                new UpdatePricingCommand(PriceCode.DOWNLOAD, new BigDecimal("0.50")),
                new UpdatePricingCommand(PriceCode.RATING_5, new BigDecimal("2.00")),
                new UpdatePricingCommand(PriceCode.RATING_4, new BigDecimal("1.50")),
                new UpdatePricingCommand(PriceCode.REVIEW_5, new BigDecimal("3.00")),
                new UpdatePricingCommand(PriceCode.REVIEW_4, new BigDecimal("2.50"))
        ));

        assertThat(repository.findByCode(PriceCode.KEYWORD_INSTALL).orElseThrow().getUnitPrice())
                .isEqualByComparingTo("1.20");
        assertThat(repository.findByCode(PriceCode.KEYWORD_INSTALL).orElseThrow().getChinaUnitPrice())
                .isEqualByComparingTo("1.80");
        assertThat(repository.findByCode(PriceCode.REVIEW_4).orElseThrow().getUnitPrice())
                .isEqualByComparingTo("2.50");
    }

    @Test
    void updatePricingRejectsNegativePrice() {
        assertThatThrownBy(() -> pricingService.updatePricing(List.of(
                new UpdatePricingCommand(PriceCode.KEYWORD_INSTALL, new BigDecimal("-0.01"))
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PRICE_INVALID);
    }

    private static class InMemoryPricingConfigRepository implements PricingConfigRepository {
        private final Map<PriceCode, PricingConfig> prices = new EnumMap<>(PriceCode.class);

        private InMemoryPricingConfigRepository() {
            for (PriceCode code : PriceCode.values()) {
                prices.put(code, PricingConfig.enabled(code, BigDecimal.ZERO));
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
