package com.youou.aso.modules.pricing.domain;

import com.youou.aso.modules.order.domain.OrderType;
import java.math.BigDecimal;

public record OrderModuleConfig(Long id,
                                String moduleName, String moduleNameEn, String moduleNameRu, String moduleNamePt, String moduleNameEs,
                                String moduleDescription, String moduleDescriptionEn, String moduleDescriptionRu,
                                String moduleDescriptionPt, String moduleDescriptionEs,
                                OrderType orderType,
                                BigDecimal unitPrice, BigDecimal chinaUnitPrice,
                                boolean enabled, int sortOrder) {
    public BigDecimal priceFor(String regionCode) {
        return "CN".equalsIgnoreCase(regionCode) ? chinaUnitPrice : unitPrice;
    }
}
