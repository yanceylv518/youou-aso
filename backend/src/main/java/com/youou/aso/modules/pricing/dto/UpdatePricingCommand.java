package com.youou.aso.modules.pricing.dto;

import com.youou.aso.modules.pricing.domain.PriceCode;

import java.math.BigDecimal;

public record UpdatePricingCommand(
        PriceCode code,
        BigDecimal unitPrice
) {
}
