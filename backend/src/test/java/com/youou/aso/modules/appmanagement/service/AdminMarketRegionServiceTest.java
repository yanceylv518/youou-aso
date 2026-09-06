package com.youou.aso.modules.appmanagement.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.appmanagement.domain.MarketRegion;
import com.youou.aso.modules.appmanagement.dto.AdminMarketRegionResult;
import com.youou.aso.modules.appmanagement.repository.MarketRegionRepository;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminMarketRegionServiceTest {
    private final FakeMarketRegionRepository repository = new FakeMarketRegionRepository();
    private final AdminMarketRegionService service = new AdminMarketRegionService(repository);

    @Test
    void updateRegionCanRenameCode() {
        repository.regions.put("US", region("US"));

        AdminMarketRegionResult updated = service.updateRegion(
                "US", "AA", "测试地区", "Test Region", "Test Region", "Região de teste", "Región de prueba",
                true, true, true, true, 8
        );

        assertThat(updated.code()).isEqualTo("AA");
        assertThat(repository.renamedFrom).isEqualTo("US");
        assertThat(repository.findByCode("US")).isEmpty();
        assertThat(repository.findByCode("AA")).isPresent();
    }

    @Test
    void updateRegionRejectsExistingCode() {
        repository.regions.put("US", region("US"));
        repository.regions.put("JP", region("JP"));

        assertThatThrownBy(() -> service.updateRegion(
                "US", "JP", "日本", "Japan", "Japan", "Japão", "Japón",
                true, true, true, true, 8
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }

    private MarketRegion region(String code) {
        MarketRegion region = new MarketRegion();
        region.setCode(code);
        region.setNameZh(code);
        region.setNameEn(code);
        region.setNameRu(code);
        region.setNamePt(code);
        region.setNameEs(code);
        region.setEnabled(true);
        region.setSupportsAppStore(true);
        region.setSupportsGooglePlay(true);
        region.setSupportsIpadStore(true);
        return region;
    }

    private static final class FakeMarketRegionRepository implements MarketRegionRepository {
        private final Map<String, MarketRegion> regions = new LinkedHashMap<>();
        private String renamedFrom;

        @Override
        public Optional<MarketRegion> findByCode(String code) {
            return Optional.ofNullable(regions.get(code));
        }

        @Override
        public List<MarketRegion> findAll() {
            return List.copyOf(regions.values());
        }

        @Override
        public List<MarketRegion> findEnabled() {
            return findAll();
        }

        @Override
        public void update(MarketRegion region) {
            regions.put(region.getCode(), region);
        }

        @Override
        public void renameAndUpdate(String originalCode, MarketRegion region) {
            renamedFrom = originalCode;
            regions.remove(originalCode);
            regions.put(region.getCode(), region);
        }
    }
}
