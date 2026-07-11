package com.youou.aso.modules.appmanagement.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.appmanagement.dto.MarketRegionResult;
import com.youou.aso.modules.appmanagement.repository.MarketRegionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
public class MarketRegionController {
    private final MarketRegionRepository marketRegionRepository;

    public MarketRegionController(MarketRegionRepository marketRegionRepository) {
        this.marketRegionRepository = marketRegionRepository;
    }

    @GetMapping("/enabled")
    public ApiResponse<List<MarketRegionResult>> enabled(@RequestParam(required = false) StoreType storeType) {
        return ApiResponse.ok(marketRegionRepository.findEnabled().stream()
                .filter(region -> storeType == null || region.supports(storeType))
                .map(region -> new MarketRegionResult(
                        region.getCode(),
                        region.getNameZh(),
                        region.getNameEn(),
                        region.isSupportsAppStore(),
                        region.isSupportsGooglePlay(),
                        region.isSupportsIpadStore()
                ))
                .toList());
    }
}
