package com.youou.aso.modules.appmanagement.dto;

import com.youou.aso.modules.appmanagement.domain.StoreType;

public record SearchStoreAppCommand(
        StoreType storeType,
        String regionCode,
        String keyword,
        int limit
) {
}
