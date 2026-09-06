package com.youou.aso.modules.appmanagement.dto;

import com.youou.aso.modules.appmanagement.domain.MarketRegion;

public record AdminMarketRegionResult(
        String code,
        String nameZh,
        String nameEn,
        String nameRu, String namePt, String nameEs,
        boolean enabled,
        boolean supportsAppStore,
        boolean supportsGooglePlay,
        boolean supportsIpadStore,
        int sortOrder
) {
    public static AdminMarketRegionResult from(MarketRegion region) {
        return new AdminMarketRegionResult(
                region.getCode(),
                region.getNameZh(),
                region.getNameEn(),
                region.getNameRu(), region.getNamePt(), region.getNameEs(),
                region.isEnabled(),
                region.isSupportsAppStore(),
                region.isSupportsGooglePlay(),
                region.isSupportsIpadStore(),
                region.getSortOrder()
        );
    }
}
