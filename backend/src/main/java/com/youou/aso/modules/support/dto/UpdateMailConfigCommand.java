package com.youou.aso.modules.support.dto;

public record UpdateMailConfigCommand(
        String smtpHost,
        Integer smtpPort,
        String username,
        String password,
        boolean keepExistingPassword,
        String fromAddress,
        boolean smtpAuth,
        boolean startTlsEnabled,
        boolean sslEnabled,
        String orderNotificationRecipients,
        boolean orderNotificationEnabled
) {
}
