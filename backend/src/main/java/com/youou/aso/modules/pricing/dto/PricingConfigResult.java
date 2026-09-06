package com.youou.aso.modules.pricing.dto;

import com.youou.aso.modules.pricing.domain.PriceCode;

import java.math.BigDecimal;

public record PricingConfigResult(
        PriceCode code,
        BigDecimal unitPrice,
        BigDecimal chinaUnitPrice,
        boolean enabled
) {
}