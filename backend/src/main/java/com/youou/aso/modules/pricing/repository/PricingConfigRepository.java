package com.youou.aso.modules.pricing.repository;

import com.youou.aso.modules.pricing.domain.PriceCode;
import com.youou.aso.modules.pricing.domain.PricingConfig;

import java.util.List;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import com.youou.aso.modules.order.domain.OrderType;

public interface PricingConfigRepository {
    List<PricingConfig> findAll();

    Optional<PricingConfig> findByCode(PriceCode code);

    void saveAll(List<PricingConfig> configs);

    default List<String> findModuleRegions(Long moduleId) { return List.of(); }
    default Map<String, BigDecimal> findModulePrices(Long moduleId, PriceCode code) { return Map.of(); }
    default void replaceModuleRegions(Long moduleId, List<String> regions) { }
    default void replaceModulePrices(Long moduleId, PriceCode code, Map<String, BigDecimal> prices) { }

    default List<String> findAllowedRegionCodes(OrderType orderType) { return List.of(); }

    default boolean hasRegionConfiguration(OrderType orderType) { return false; }

    default Map<String, BigDecimal> findRegionPrices(PriceCode code) { return Map.of(); }

    default Optional<BigDecimal> findRegionPrice(PriceCode code, String regionCode) { return Optional.empty(); }

    default void replaceAllowedRegions(OrderType orderType, List<String> regionCodes) { }

    default void replaceRegionPrices(PriceCode code, Map<String, BigDecimal> regionPrices) { }
}
