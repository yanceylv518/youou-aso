package com.youou.aso.modules.appmanagement.repository;

import com.youou.aso.modules.appmanagement.domain.CustomerApp;
import com.youou.aso.modules.appmanagement.domain.CustomerAppStatus;
import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.appmanagement.dto.CustomerAppQuery;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CustomerAppRepository {
    Optional<CustomerApp> findById(Long id);

    default Optional<CustomerApp> findActiveByCustomerStoreAndIdentifier(Long customerId, StoreType storeType, String appIdentifier) {
        return Optional.empty();
    }

    boolean existsByCustomerAndStoreAndRegionAndIdentifier(Long customerId, StoreType storeType, String regionCode, String appIdentifier);

    boolean existsByCustomerAndStoreAndRegionAndIdentifierExcludingId(Long customerId, StoreType storeType, String regionCode, String appIdentifier, Long excludedId);

    CustomerApp save(CustomerApp app);

    default void addRegion(Long customerAppId, String regionCode) {
    }

    default boolean existsRegion(Long customerAppId, String regionCode) {
        return false;
    }

    default Map<Long, List<String>> findRegionCodesByAppIds(List<Long> appIds) {
        return Map.of();
    }

    CustomerApp update(CustomerApp app);

    void updateStatus(Long id, CustomerAppStatus status);

    List<CustomerApp> findByCustomerId(Long customerId, CustomerAppQuery query);

    List<CustomerApp> findAll(CustomerAppQuery query);

    default List<CustomerApp> findByCustomerId(Long customerId, CustomerAppQuery query, int limit, int offset) {
        return findByCustomerId(customerId, query).stream().skip(offset).limit(limit).toList();
    }

    default long countByCustomerId(Long customerId, CustomerAppQuery query) {
        return findByCustomerId(customerId, query).size();
    }

    default List<CustomerApp> findAll(CustomerAppQuery query, int limit, int offset) {
        return findAll(query).stream().skip(offset).limit(limit).toList();
    }

    default long countAll(CustomerAppQuery query) {
        return findAll(query).size();
    }
}
