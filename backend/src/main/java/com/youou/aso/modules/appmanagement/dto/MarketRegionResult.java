package com.youou.aso.modules.appmanagement.dto;

public record MarketRegionResult(
        String code,
        String nameZh,
        String nameEn,
        boolean supportsAppStore,
        boolean supportsGooglePlay,
        boolean supportsIpadStore
) {
}
