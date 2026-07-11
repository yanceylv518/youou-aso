package com.youou.aso.modules.support.service;

import com.youou.aso.modules.support.domain.SystemConfig;
import com.youou.aso.modules.support.repository.SystemConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SystemConfigService {
    private final SystemConfigRepository repository;

    public SystemConfigService(SystemConfigRepository repository) {
        this.repository = repository;
    }

    public Optional<String> getValue(String key) {
        return repository.findByKey(key)
                .map(SystemConfig::getValue)
                .filter(value -> !value.isBlank());
    }

    public Map<String, SystemConfig> getConfigs(Collection<String> keys) {
        return repository.findByKeys(keys).stream()
                .collect(Collectors.toMap(SystemConfig::getKey, Function.identity()));
    }

    @Transactional
    public SystemConfig save(String key, String value, boolean secret, String description) {
        return repository.save(key, normalizeBlank(value), secret, description);
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
