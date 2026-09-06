package com.youou.aso.modules.support.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.support.domain.CustomerServiceConfig;
import com.youou.aso.modules.support.dto.CustomerServiceConfigResult;
import com.youou.aso.modules.support.dto.UpdateCustomerServiceConfigCommand;
import com.youou.aso.modules.support.repository.CustomerServiceConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
public class CustomerServiceConfigService {
    private static final String DEFAULT_SERVICE_NAME = "Youou-ASO Support";
    private static final String DEFAULT_CONTACT_HINT = "Scan the QR code to contact customer service for recharge.";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final CustomerServiceConfigRepository repository;

    public CustomerServiceConfigService(CustomerServiceConfigRepository repository) {
        this.repository = repository;
    }

    public CustomerServiceConfigResult getConfig() {
        return repository.find()
                .map(this::toResult)
                .orElseGet(() -> new CustomerServiceConfigResult(
                        DEFAULT_SERVICE_NAME,
                        null,
                        DEFAULT_CONTACT_HINT,
                        null,
                        false,
                        null,
                        false,
                        null,
                        null,
                        null,
                        false,
                        null,
                        false,
                        null,
                        false,
                        null
                ));
    }

    @Transactional
    public CustomerServiceConfigResult updateConfig(UpdateCustomerServiceConfigCommand command) {
        String serviceName = normalizeText(command.serviceName(), DEFAULT_SERVICE_NAME, 120);
        String qrCodeUrl = normalizeOptionalUrl(command.qrCodeUrl());
        String contactHint = normalizeText(command.contactHint(), DEFAULT_CONTACT_HINT, 512);
        String email = normalizeOptionalEmail(command.email());
        String phone = normalizeOptionalText(command.phone(), 40);
        String teamsUrl = normalizeOptionalUrl(command.teamsUrl());
        String telegramUrl = normalizeOptionalUrl(command.telegramUrl());
        String telegramQrUrl = normalizeOptionalUrl(command.telegramQrUrl());
        String wechatQrUrl = normalizeOptionalUrl(command.wechatQrUrl());
        String whatsappUrl = normalizeOptionalUrl(command.whatsappUrl());
        if ((command.emailVisible() && email == null)
                || (command.phoneVisible() && phone == null)
                || (command.telegramQrVisible() && telegramQrUrl == null)
                || (command.wechatQrVisible() && wechatQrUrl == null)) {
            throw new BusinessException(ErrorCode.CONFIG_VALUE_INVALID);
        }

        CustomerServiceConfig config = new CustomerServiceConfig();
        config.setServiceName(serviceName);
        config.setQrCodeUrl(qrCodeUrl);
        config.setContactHint(contactHint);
        config.setEmail(email);
        config.setEmailVisible(command.emailVisible());
        config.setPhone(phone);
        config.setPhoneVisible(command.phoneVisible());
        config.setTeamsUrl(teamsUrl);
        config.setTelegramUrl(telegramUrl);
        config.setTelegramQrUrl(telegramQrUrl);
        config.setTelegramQrVisible(command.telegramQrVisible());
        config.setWechatQrUrl(wechatQrUrl);
        config.setWechatQrVisible(command.wechatQrVisible());
        config.setWhatsappUrl(whatsappUrl);
        config.setEnabled(command.enabled());
        return toResult(repository.save(config));
    }

    private CustomerServiceConfigResult toResult(CustomerServiceConfig config) {
        return new CustomerServiceConfigResult(
                config.getServiceName(),
                config.getQrCodeUrl(),
                config.getContactHint(),
                config.getEmail(),
                config.isEmailVisible(),
                config.getPhone(),
                config.isPhoneVisible(),
                config.getTeamsUrl(),
                config.getTelegramUrl(),
                config.getTelegramQrUrl(),
                config.isTelegramQrVisible(),
                config.getWechatQrUrl(),
                config.isWechatQrVisible(),
                config.getWhatsappUrl(),
                config.isEnabled(),
                formatDateTime(config.getUpdatedAt())
        );
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.toString();
    }

    private String normalizeText(String value, String fallback, int maxLength) {
        String normalized = value == null || value.isBlank() ? fallback : value.trim();
        if (normalized.length() > maxLength) {
            throw new BusinessException(ErrorCode.CONFIG_VALUE_INVALID);
        }
        return normalized;
    }

    private String normalizeOptionalUrl(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > 1024) {
            throw new BusinessException(ErrorCode.CONFIG_VALUE_INVALID);
        }
        if (normalized.startsWith("/uploads/app-icons/") && !normalized.contains("..")) {
            return normalized;
        }
        try {
            URI uri = new URI(normalized);
            String scheme = uri.getScheme();
            if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                throw new BusinessException(ErrorCode.CONFIG_VALUE_INVALID);
            }
            return normalized;
        } catch (URISyntaxException exception) {
            throw new BusinessException(ErrorCode.CONFIG_VALUE_INVALID);
        }
    }

    private String normalizeOptionalEmail(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > 254 || !EMAIL_PATTERN.matcher(normalized).matches()) {
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
}
