package com.youou.aso.modules.support.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.support.domain.SystemConfig;
import com.youou.aso.modules.support.dto.MailConfigResult;
import com.youou.aso.modules.support.dto.UpdateMailConfigCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class MailConfigService {
    public static final String KEY_SMTP_HOST = "mail.smtp.host";
    public static final String KEY_SMTP_PORT = "mail.smtp.port";
    public static final String KEY_USERNAME = "mail.smtp.username";
    public static final String KEY_PASSWORD = "mail.smtp.password";
    public static final String KEY_FROM = "mail.from";
    public static final String KEY_SMTP_AUTH = "mail.smtp.auth";
    public static final String KEY_STARTTLS = "mail.smtp.starttls.enable";
    public static final String KEY_SSL = "mail.smtp.ssl.enable";
    public static final String KEY_ORDER_NOTIFICATION_ENABLED = "order.notification.enabled";
    public static final String KEY_ORDER_NOTIFICATION_RECIPIENTS = "order.notification.recipients";

    private static final List<String> MAIL_KEYS = List.of(
            KEY_SMTP_HOST,
            KEY_SMTP_PORT,
            KEY_USERNAME,
            KEY_PASSWORD,
            KEY_FROM,
            KEY_SMTP_AUTH,
            KEY_STARTTLS,
            KEY_SSL,
            KEY_ORDER_NOTIFICATION_ENABLED,
            KEY_ORDER_NOTIFICATION_RECIPIENTS
    );
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final SystemConfigService systemConfigService;
    private final String fallbackHost;
    private final Integer fallbackPort;
    private final String fallbackUsername;
    private final String fallbackPassword;
    private final String fallbackFrom;
    private final boolean fallbackSmtpAuth;
    private final boolean fallbackStartTls;
    private final boolean fallbackSsl;

    public MailConfigService(
            SystemConfigService systemConfigService,
            @Value("${spring.mail.host:}") String fallbackHost,
            @Value("${spring.mail.port:587}") Integer fallbackPort,
            @Value("${spring.mail.username:}") String fallbackUsername,
            @Value("${spring.mail.password:}") String fallbackPassword,
            @Value("${youou.mail.from:${spring.mail.username:}}") String fallbackFrom,
            @Value("${spring.mail.properties.mail.smtp.auth:true}") boolean fallbackSmtpAuth,
            @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}") boolean fallbackStartTls,
            @Value("${spring.mail.properties.mail.smtp.ssl.enable:false}") boolean fallbackSsl
    ) {
        this.systemConfigService = systemConfigService;
        this.fallbackHost = fallbackHost;
        this.fallbackPort = fallbackPort;
        this.fallbackUsername = fallbackUsername;
        this.fallbackPassword = fallbackPassword;
        this.fallbackFrom = fallbackFrom;
        this.fallbackSmtpAuth = fallbackSmtpAuth;
        this.fallbackStartTls = fallbackStartTls;
        this.fallbackSsl = fallbackSsl;
    }

    public MailConfigResult getConfig() {
        Map<String, SystemConfig> configs = systemConfigService.getConfigs(MAIL_KEYS);
        MailSettings settings = resolveSettings(configs);
        return new MailConfigResult(
                settings.smtpHost(),
                settings.smtpPort(),
                settings.username(),
                settings.fromAddress(),
                settings.smtpAuth(),
                settings.startTlsEnabled(),
                settings.sslEnabled(),
                hasText(settings.password()),
                value(configs, KEY_ORDER_NOTIFICATION_RECIPIENTS),
                boolValue(configs, KEY_ORDER_NOTIFICATION_ENABLED, false),
                configs.values().stream()
                        .map(SystemConfig::getUpdatedAt)
                        .filter(java.util.Objects::nonNull)
                        .max(java.time.LocalDateTime::compareTo)
                        .map(java.time.LocalDateTime::toString)
                        .orElse(null)
        );
    }

    public MailSettings getMailSettings() {
        return resolveSettings(systemConfigService.getConfigs(MAIL_KEYS));
    }

    public String[] getEnabledOrderNotificationRecipients() {
        Map<String, SystemConfig> configs = systemConfigService.getConfigs(List.of(
                KEY_ORDER_NOTIFICATION_ENABLED,
                KEY_ORDER_NOTIFICATION_RECIPIENTS
        ));
        if (!boolValue(configs, KEY_ORDER_NOTIFICATION_ENABLED, false)) {
            return new String[0];
        }
        return parseRecipients(value(configs, KEY_ORDER_NOTIFICATION_RECIPIENTS));
    }

    @Transactional
    public MailConfigResult updateConfig(UpdateMailConfigCommand command) {
        String smtpHost = normalizeOptionalText(command.smtpHost(), 255);
        String username = normalizeOptionalText(command.username(), 255);
        String fromAddress = normalizeOptionalEmail(command.fromAddress());
        String recipients = normalizeRecipients(command.orderNotificationRecipients());
        Integer smtpPort = normalizePort(command.smtpPort());
        if (command.orderNotificationEnabled() && recipients == null) {
            throw new BusinessException(ErrorCode.CONFIG_VALUE_INVALID);
        }

        systemConfigService.save(KEY_SMTP_HOST, smtpHost, false, "SMTP server host");
        systemConfigService.save(KEY_SMTP_PORT, String.valueOf(smtpPort), false, "SMTP server port");
        systemConfigService.save(KEY_USERNAME, username, false, "SMTP username");
        if (!command.keepExistingPassword()) {
            systemConfigService.save(KEY_PASSWORD, normalizeOptionalText(command.password(), 512), true, "SMTP password or app password");
        }
        systemConfigService.save(KEY_FROM, fromAddress, false, "Default sender email address");
        systemConfigService.save(KEY_SMTP_AUTH, String.valueOf(command.smtpAuth()), false, "Enable SMTP authentication");
        systemConfigService.save(KEY_STARTTLS, String.valueOf(command.startTlsEnabled()), false, "Enable SMTP STARTTLS");
        systemConfigService.save(KEY_SSL, String.valueOf(command.sslEnabled()), false, "Enable SMTP SSL");
        systemConfigService.save(KEY_ORDER_NOTIFICATION_ENABLED, String.valueOf(command.orderNotificationEnabled()), false, "Enable order success email notifications");
        systemConfigService.save(KEY_ORDER_NOTIFICATION_RECIPIENTS, recipients, false, "Order success notification recipients");
        return getConfig();
    }

    private MailSettings resolveSettings(Map<String, SystemConfig> configs) {
        String host = firstNonBlank(value(configs, KEY_SMTP_HOST), fallbackHost);
        Integer port = parsePort(firstNonBlank(value(configs, KEY_SMTP_PORT), fallbackPort == null ? null : String.valueOf(fallbackPort)));
        String username = firstNonBlank(value(configs, KEY_USERNAME), fallbackUsername);
        String password = firstNonBlank(value(configs, KEY_PASSWORD), fallbackPassword);
        String from = firstNonBlank(value(configs, KEY_FROM), fallbackFrom, username);
        return new MailSettings(
                host,
                port == null ? 587 : port,
                username,
                password,
                from,
                boolValue(configs, KEY_SMTP_AUTH, fallbackSmtpAuth),
                boolValue(configs, KEY_STARTTLS, fallbackStartTls),
                boolValue(configs, KEY_SSL, fallbackSsl)
        );
    }

    private String normalizeRecipients(String rawRecipients) {
        String[] recipients = parseRecipients(rawRecipients);
        if (recipients.length == 0) {
            return null;
        }
        String normalized = String.join(",", recipients);
        if (normalized.length() > 1024) {
            throw new BusinessException(ErrorCode.CONFIG_VALUE_INVALID);
        }
        return normalized;
    }

    private String[] parseRecipients(String rawRecipients) {
        if (rawRecipients == null || rawRecipients.isBlank()) {
            return new String[0];
        }
        String[] recipients = Arrays.stream(rawRecipients.split("[,;\\n\\r]+"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toArray(String[]::new);
        for (String recipient : recipients) {
            if (!EMAIL_PATTERN.matcher(recipient).matches()) {
                throw new BusinessException(ErrorCode.CONFIG_VALUE_INVALID);
            }
        }
        return recipients;
    }

    private String normalizeOptionalEmail(String value) {
        String normalized = normalizeOptionalText(value, 255);
        if (normalized != null && !EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException(ErrorCode.CONFIG_VALUE_INVALID);
        }
        return normalized;
    }

    private String normalizeOptionalText(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new BusinessException(ErrorCode.CONFIG_VALUE_INVALID);
        }
        return normalized;
    }

    private Integer normalizePort(Integer value) {
        if (value == null || value < 1 || value > 65535) {
            throw new BusinessException(ErrorCode.CONFIG_VALUE_INVALID);
        }
        return value;
    }

    private Integer parsePort(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return normalizePort(Integer.parseInt(value.trim()));
        } catch (NumberFormatException exception) {
            return 587;
        }
    }

    private String value(Map<String, SystemConfig> configs, String key) {
        SystemConfig config = configs.get(key);
        return config == null ? null : config.getValue();
    }

    private boolean boolValue(Map<String, SystemConfig> configs, String key, boolean fallback) {
        String value = value(configs, key);
        return hasText(value) ? Boolean.parseBoolean(value) : fallback;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public record MailSettings(
            String smtpHost,
            Integer smtpPort,
            String username,
            String password,
            String fromAddress,
            boolean smtpAuth,
            boolean startTlsEnabled,
            boolean sslEnabled
    ) {
        public boolean hasSmtpServer() {
            return smtpHost != null && !smtpHost.isBlank();
        }
    }
}
