package com.youou.aso.modules.support.dto;

public record MailConfigResult(
        String smtpHost,
        Integer smtpPort,
        String username,
        String fromAddress,
        boolean smtpAuth,
        boolean startTlsEnabled,
        boolean sslEnabled,
        boolean passwordConfigured,
        String orderNotificationRecipients,
        boolean orderNotificationEnabled,
        String updatedAt
) {
}
