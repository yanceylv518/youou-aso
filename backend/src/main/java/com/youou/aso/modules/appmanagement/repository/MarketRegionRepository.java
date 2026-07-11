package com.youou.aso.modules.appmanagement.repository;

import com.youou.aso.modules.appmanagement.domain.MarketRegion;

import java.util.List;
import java.util.Optional;

public interface MarketRegionRepository {
    Optional<MarketRegion> findByCode(String code);

    List<MarketRegion> findAll();

    List<MarketRegion> findEnabled();

    void update(MarketRegion region);
}
