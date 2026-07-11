package com.youou.aso.modules.support.repository;

import com.youou.aso.modules.support.domain.SystemConfig;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SystemConfigRepository {
    Optional<SystemConfig> findByKey(String key);

    List<SystemConfig> findByKeys(Collection<String> keys);

    SystemConfig save(String key, String value, boolean secret, String description);
}
