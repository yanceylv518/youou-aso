package com.youou.aso.modules.pricing.repository;

import com.youou.aso.modules.pricing.domain.PriceCode;
import com.youou.aso.modules.pricing.domain.PricingConfig;

import java.util.List;
import java.util.Optional;

public interface PricingConfigRepository {
    List<PricingConfig> findAll();

    Optional<PricingConfig> findByCode(PriceCode code);

    void saveAll(List<PricingConfig> configs);
}
