package com.youou.aso.modules.appmanagement.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.appmanagement.domain.MarketRegion;
import com.youou.aso.modules.appmanagement.dto.AdminMarketRegionResult;
import com.youou.aso.modules.appmanagement.repository.MarketRegionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class AdminMarketRegionService {
    private final MarketRegionRepository marketRegionRepository;

    public AdminMarketRegionService(MarketRegionRepository marketRegionRepository) {
        this.marketRegionRepository = marketRegionRepository;
    }

    public List<AdminMarketRegionResult> listRegions() {
        return marketRegionRepository.findAll().stream().map(AdminMarketRegionResult::from).toList();
    }

    public AdminMarketRegionResult createRegion(String code, String nameZh, String nameEn, String nameRu, String namePt, String nameEs, boolean enabled,
            boolean supportsAppStore, boolean supportsGooglePlay, boolean supportsIpadStore, int sortOrder) {
        String normalizedCode = normalizeCode(code);
        if (!normalizedCode.matches("[A-Z]{2}") || marketRegionRepository.findByCode(normalizedCode).isPresent()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        MarketRegion region = new MarketRegion();
        region.setCode(normalizedCode);
        applyEditableFields(region, nameZh, nameEn, nameRu, namePt, nameEs, enabled, supportsAppStore, supportsGooglePlay, supportsIpadStore, sortOrder);
        return AdminMarketRegionResult.from(marketRegionRepository.save(region));
    }

    @Transactional
    public AdminMarketRegionResult updateRegion(String originalCode, String code, String nameZh, String nameEn, String nameRu, String namePt, String nameEs, boolean enabled,
            boolean supportsAppStore, boolean supportsGooglePlay, boolean supportsIpadStore, int sortOrder) {
        String normalizedOriginalCode = normalizeCode(originalCode);
        String normalizedCode = normalizeCode(code);
        if (!normalizedCode.matches("[A-Z]{2}")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        MarketRegion region = marketRegionRepository.findByCode(normalizedOriginalCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (!normalizedOriginalCode.equals(normalizedCode) && marketRegionRepository.findByCode(normalizedCode).isPresent()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        region.setCode(normalizedCode);
        applyEditableFields(region, nameZh, nameEn, nameRu, namePt, nameEs, enabled, supportsAppStore, supportsGooglePlay, supportsIpadStore, sortOrder);
        marketRegionRepository.renameAndUpdate(normalizedOriginalCode, region);
        return AdminMarketRegionResult.from(region);
    }

    private void applyEditableFields(MarketRegion region, String nameZh, String nameEn, String nameRu, String namePt, String nameEs, boolean enabled,
            boolean supportsAppStore, boolean supportsGooglePlay, boolean supportsIpadStore, int sortOrder) {
        if (isBlank(nameZh) || isBlank(nameEn) || isBlank(nameRu) || isBlank(namePt) || isBlank(nameEs) || sortOrder < 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        region.setNameZh(nameZh.trim());
        region.setNameEn(nameEn.trim());
        region.setNameRu(nameRu.trim()); region.setNamePt(namePt.trim()); region.setNameEs(nameEs.trim());
        region.setEnabled(enabled);
        region.setSupportsAppStore(supportsAppStore);
        region.setSupportsGooglePlay(supportsGooglePlay);
        region.setSupportsIpadStore(supportsIpadStore);
        region.setSortOrder(sortOrder);
    }
    private boolean isBlank(String value) { return value == null || value.isBlank(); }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }
}
