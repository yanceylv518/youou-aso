package com.youou.aso.modules.support.dto;

public record CustomerServiceConfigResult(
        String serviceName,
        String qrCodeUrl,
        String contactHint,
        boolean enabled,
        String updatedAt
) {
}
