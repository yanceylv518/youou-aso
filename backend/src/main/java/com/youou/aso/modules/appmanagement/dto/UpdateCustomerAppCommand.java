package com.youou.aso.modules.appmanagement.dto;

import com.youou.aso.modules.appmanagement.domain.StoreType;

public record UpdateCustomerAppCommand(
        StoreType storeType,
        String regionCode,
        String appIdentifier
) {
}
