package com.youou.aso.modules.support.dto;

public record UpdateCustomerServiceConfigCommand(
        String serviceName,
        String qrCodeUrl,
        String contactHint,
        boolean enabled
) {
}
