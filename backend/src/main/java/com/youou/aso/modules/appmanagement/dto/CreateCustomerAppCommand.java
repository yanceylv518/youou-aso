package com.youou.aso.modules.appmanagement.dto;

import com.youou.aso.modules.appmanagement.domain.StoreType;

public record CreateCustomerAppCommand(
        StoreType storeType,
        String regionCode,
        String appIdentifier,
        String categoryHint
) {
    public CreateCustomerAppCommand(StoreType storeType, String regionCode, String appIdentifier) {
        this(storeType, regionCode, appIdentifier, null);
    }
}
