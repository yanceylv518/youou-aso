package com.youou.aso.modules.support.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.support.domain.SystemConfig;
import com.youou.aso.modules.support.dto.HomeMetricsResult;
import com.youou.aso.modules.support.dto.UpdateHomeMetricsCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class HomeMetricsService {
    private static final String APPS_KEY = "home.metrics.apps.value";
    private static final String SATISFACTION_KEY = "home.metrics.satisfaction.value";
    private static final String EXPERIENCE_KEY = "home.metrics.experience.years";
    private static final String TEAM_KEY = "home.metrics.team.value";
    private static final List<String> KEYS = List.of(APPS_KEY, SATISFACTION_KEY, EXPERIENCE_KEY, TEAM_KEY);

    private final SystemConfigService systemConfigService;

    public HomeMetricsService(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    public HomeMetricsResult getConfig() {
        return toResult(systemConfigService.getConfigs(KEYS));
    }

    @Transactional
    public HomeMetricsResult updateConfig(UpdateHomeMetricsCommand command) {
        String appsValue = normalizeDisplayValue(command.appsValue(), "10,000+");
        String satisfactionValue = normalizeDisplayValue(command.satisfactionValue(), "98.6%");
        String teamValue = normalizeDisplayValue(command.teamValue(), "50+");
        if (command.experienceYears() < 0 || command.experienceYears() > 999) {
            throw new BusinessException(ErrorCode.CONFIG_VALUE_INVALID);
        }
        systemConfigService.save(APPS_KEY, appsValue, false, "Public home served apps metric");
        systemConfigService.save(SATISFACTION_KEY, satisfactionValue, false, "Public home satisfaction metric");
        systemConfigService.save(EXPERIENCE_KEY, String.valueOf(command.experienceYears()), false, "Public home experience years");
        systemConfigService.save(TEAM_KEY, teamValue, false, "Public home team metric");
        return getConfig();
    }

    private HomeMetricsResult toResult(Map<String, SystemConfig> configs) {
        LocalDateTime updatedAt = configs.values().stream()
                .map(SystemConfig::getUpdatedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        return new HomeMetricsResult(
                value(configs, APPS_KEY, "10,000+"),
                value(configs, SATISFACTION_KEY, "98.6%"),
                intValue(configs, EXPERIENCE_KEY, 5),
                value(configs, TEAM_KEY, "50+"),
                updatedAt == null ? null : updatedAt.toString()
        );
    }

    private String value(Map<String, SystemConfig> configs, String key, String fallback) {
        SystemConfig config = configs.get(key);
        return config == null || config.getValue() == null || config.getValue().isBlank()
                ? fallback
                : config.getValue();
    }

    private int intValue(Map<String, SystemConfig> configs, String key, int fallback) {
        try {
            return Integer.parseInt(value(configs, key, String.valueOf(fallback)));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private String normalizeDisplayValue(String value, String fallback) {
        String normalized = value == null || value.isBlank() ? fallback : value.trim();
        if (normalized.length() > 32) {
            throw new BusinessException(ErrorCode.CONFIG_VALUE_INVALID);
        }
        return normalized;
    }
}
