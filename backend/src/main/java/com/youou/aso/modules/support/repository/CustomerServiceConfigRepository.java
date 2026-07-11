package com.youou.aso.modules.support.repository;

import com.youou.aso.modules.support.domain.CustomerServiceConfig;

import java.util.Optional;

public interface CustomerServiceConfigRepository {
    Optional<CustomerServiceConfig> find();

    CustomerServiceConfig save(CustomerServiceConfig config);
}
