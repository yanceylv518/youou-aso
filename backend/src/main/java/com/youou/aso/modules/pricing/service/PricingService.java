package com.youou.aso.modules.pricing.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.pricing.domain.PriceCode;
import com.youou.aso.modules.pricing.domain.PricingConfig;
import com.youou.aso.modules.pricing.dto.PricingConfigResult;
import com.youou.aso.modules.pricing.dto.UpdatePricingCommand;
import com.youou.aso.modules.pricing.repository.PricingConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class PricingService {
    private final PricingConfigRepository pricingConfigRepository;

    public PricingService(PricingConfigRepository pricingConfigRepository) {
        this.pricingConfigRepository = pricingConfigRepository;
    }

    public List<PricingConfig> listPricing() {
        Map<PriceCode, PricingConfig> existing = new EnumMap<>(PriceCode.class);
        pricingConfigRepository.findAll().forEach(config -> existing.put(config.getCode(), config));
        return List.of(PriceCode.values()).stream()
                .map(code -> existing.getOrDefault(code, PricingConfig.enabled(code, BigDecimal.ZERO)))
                .sorted(Comparator.comparingInt(config -> config.getCode().ordinal()))
                .toList();
    }

    public List<PricingConfigResult> listPricingResults() {
        return listPricing().stream()
                .map(config -> new PricingConfigResult(config.getCode(), config.getUnitPrice(), config.isEnabled()))
                .toList();
    }

    @Transactional
    public List<PricingConfigResult> updatePricing(List<UpdatePricingCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new BusinessException(ErrorCode.PRICE_INVALID);
        }

        Map<PriceCode, BigDecimal> requestedPrices = new EnumMap<>(PriceCode.class);
        for (UpdatePricingCommand command : commands) {
            if (command == null || command.code() == null || command.unitPrice() == null
                    || command.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(ErrorCode.PRICE_INVALID);
            }
            requestedPrices.put(command.code(), command.unitPrice());
        }

        List<PricingConfig> updated = listPricing().stream()
                .map(config -> {
                    BigDecimal unitPrice = requestedPrices.get(config.getCode());
                    return unitPrice == null ? config : PricingConfig.enabled(config.getCode(), unitPrice);
                })
                .toList();
        pricingConfigRepository.saveAll(updated);
        return updated.stream()
                .map(config -> new PricingConfigResult(config.getCode(), config.getUnitPrice(), config.isEnabled()))
                .toList();
    }
}
