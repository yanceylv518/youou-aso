package com.youou.aso.modules.appmanagement.service;

import com.youou.aso.modules.appmanagement.domain.StoreType;

import java.util.List;
import java.util.Optional;

public interface AppStoreVerifier {
    Optional<VerifiedStoreApp> verify(StoreType storeType, String regionCode, String appIdentifier);

    List<StoreAppSearchResult> search(StoreType storeType, String regionCode, String keyword, int limit);
}
