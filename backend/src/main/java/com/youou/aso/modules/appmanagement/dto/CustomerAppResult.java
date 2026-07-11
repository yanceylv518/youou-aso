package com.youou.aso.modules.appmanagement.dto;

import com.youou.aso.modules.appmanagement.domain.StoreType;

import java.time.LocalDateTime;
import java.util.List;

public record CustomerAppResult(
        Long id,
        Long customerId,
        StoreType storeType,
        String regionCode,
        String appIdentifier,
        String appName,
        String appIconUrl,
        String bundleId,
        String externalAppId,
        String category,
        List<String> regionCodes,
        String customerUsername,
        String customerEmail,
        String status,
        LocalDateTime verifiedAt,
        LocalDateTime createdAt
) {
}
