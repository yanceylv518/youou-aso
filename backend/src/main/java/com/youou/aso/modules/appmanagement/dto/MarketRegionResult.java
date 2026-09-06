package com.youou.aso.modules.appmanagement.dto;

public record MarketRegionResult(
        String code,
        String nameZh,
        String nameEn,
        String nameRu, String namePt, String nameEs,
        boolean supportsAppStore,
        boolean supportsGooglePlay,
        boolean supportsIpadStore
) {
}
