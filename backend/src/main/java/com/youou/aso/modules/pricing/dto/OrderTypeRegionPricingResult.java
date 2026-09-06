package com.youou.aso.modules.pricing.dto;

import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.pricing.domain.PriceCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record OrderTypeRegionPricingResult(
        OrderType orderType,
        List<String> allowedRegionCodes,
        Map<PriceCode, Map<String, BigDecimal>> regionPrices
) {
}
