package com.youou.aso.modules.appmanagement.service;

import com.youou.aso.modules.appmanagement.domain.StoreType;

public record StoreAppSearchResult(
        StoreType storeType,
        String regionCode,
        String appIdentifier,
        String appName,
        String appIconUrl,
        String bundleId,
        String externalAppId,
        String category,
        String developerName
) {
}
