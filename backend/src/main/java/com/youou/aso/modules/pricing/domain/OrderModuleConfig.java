package com.youou.aso.modules.pricing.domain;

import com.youou.aso.modules.order.domain.OrderType;
import java.math.BigDecimal;
import java.util.List;
import com.youou.aso.modules.appmanagement.domain.StoreType;

public record OrderModuleConfig(Long id,
                                String moduleName, String moduleNameEn, String moduleNameRu, String moduleNamePt, String moduleNameEs,
                                String moduleDescription, String moduleDescriptionEn, String moduleDescriptionRu,
                                String moduleDescriptionPt, String moduleDescriptionEs,
                                OrderType orderType,
                                BigDecimal unitPrice, BigDecimal chinaUnitPrice,
                                boolean enabled, int sortOrder, List<StoreType> storeTypes) {
    public OrderModuleConfig(Long id, String moduleName, String moduleNameEn, String moduleNameRu, String moduleNamePt, String moduleNameEs, String moduleDescription, String moduleDescriptionEn, String moduleDescriptionRu, String moduleDescriptionPt, String moduleDescriptionEs, OrderType orderType, BigDecimal unitPrice, BigDecimal chinaUnitPrice, boolean enabled, int sortOrder) {
        this(id,moduleName,moduleNameEn,moduleNameRu,moduleNamePt,moduleNameEs,moduleDescription,moduleDescriptionEn,moduleDescriptionRu,moduleDescriptionPt,moduleDescriptionEs,orderType,unitPrice,chinaUnitPrice,enabled,sortOrder,List.of(StoreType.values()));
    }
    public BigDecimal priceFor(String regionCode) {
        return "CN".equalsIgnoreCase(regionCode) ? chinaUnitPrice : unitPrice;
    }
}
