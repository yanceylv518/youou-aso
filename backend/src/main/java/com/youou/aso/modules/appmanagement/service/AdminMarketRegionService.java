package com.youou.aso.modules.appmanagement.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.appmanagement.domain.MarketRegion;
import com.youou.aso.modules.appmanagement.dto.AdminMarketRegionResult;
import com.youou.aso.modules.appmanagement.repository.MarketRegionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminMarketRegionService {
    private final MarketRegionRepository marketRegionRepository;

    public AdminMarketRegionService(MarketRegionRepository marketRegionRepository) {
        this.marketRegionRepository = marketRegionRepository;
    }

    public List<AdminMarketRegionResult> listRegions() {
        return marketRegionRepository.findAll().stream()
                .map(AdminMarketRegionResult::from)
                .toList();
    }

    public AdminMarketRegionResult updateRegion(
            String code,
            boolean enabled,
            boolean supportsAppStore,
            boolean supportsGooglePlay,
            boolean supportsIpadStore
    ) {
        MarketRegion region = marketRegionRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        region.setEnabled(enabled);
        region.setSupportsAppStore(supportsAppStore);
        region.setSupportsGooglePlay(supportsGooglePlay);
        region.setSupportsIpadStore(supportsIpadStore);
        marketRegionRepository.update(region);
        return AdminMarketRegionResult.from(region);
    }
}
