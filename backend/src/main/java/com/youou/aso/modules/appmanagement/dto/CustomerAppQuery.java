package com.youou.aso.modules.appmanagement.dto;

import com.youou.aso.modules.appmanagement.domain.CustomerAppStatus;
import com.youou.aso.modules.appmanagement.domain.StoreType;

public record CustomerAppQuery(
        String keyword,
        StoreType storeType,
        String regionCode,
        CustomerAppStatus status
) {
}
