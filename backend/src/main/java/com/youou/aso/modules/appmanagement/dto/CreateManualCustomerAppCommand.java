package com.youou.aso.modules.appmanagement.dto;

import com.youou.aso.modules.appmanagement.domain.StoreType;

public record CreateManualCustomerAppCommand(
        StoreType storeType,
        String regionCode,
        String appIdentifier,
        String appName,
        String appIconUrl,
        String categoryHint
) {
}
