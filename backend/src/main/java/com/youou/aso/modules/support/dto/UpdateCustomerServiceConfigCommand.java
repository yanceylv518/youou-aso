package com.youou.aso.modules.support.dto;

public record UpdateCustomerServiceConfigCommand(
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
        boolean enabled
) {
    public UpdateCustomerServiceConfigCommand(
            String serviceName,
            String qrCodeUrl,
            String contactHint,
            String email,
            String teamsUrl,
            String telegramUrl,
            String whatsappUrl,
            boolean enabled
    ) {
        this(serviceName, qrCodeUrl, contactHint, email, email != null && !email.isBlank(), null, false,
                teamsUrl, telegramUrl, null, false, null, false, whatsappUrl, enabled);
    }
}
