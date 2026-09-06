package com.youou.aso.modules.support.dto;

public record CustomerServiceConfigResult(
        String serviceName,
        String qrCodeUrl,
        String contactHint,
        String email,
        boolean emailVisible,
        String phone,
        boolean phoneVisible,
        String teamsUrl,
        String telegramUrl,
        String telegramQrUrl,
        boolean telegramQrVisible,
        String wechatQrUrl,
        boolean wechatQrVisible,
        String whatsappUrl,
        boolean enabled,
        String updatedAt
) {
}
