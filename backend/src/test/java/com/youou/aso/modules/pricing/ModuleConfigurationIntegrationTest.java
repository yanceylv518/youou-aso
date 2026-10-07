package com.youou.aso.modules.pricing;

import com.youou.aso.modules.pricing.domain.*;
import com.youou.aso.modules.pricing.repository.*;
import com.youou.aso.modules.pricing.service.*;
import com.youou.aso.modules.pricing.dto.OrderTypeRegionPricingResult;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.common.error.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

@org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named = "YOUOU_TEST_MYSQL_URL", matches = "jdbc:mysql://.+")
@SpringBootTest
@Transactional
class ModuleConfigurationIntegrationTest extends com.youou.aso.support.IsolatedMysqlTest {
    @Autowired OrderModuleConfigService modules;
    @Autowired OrderModuleConfigRepository moduleRepository;
    @Autowired PricingService pricing;
    @Autowired org.springframework.context.MessageSource messages;
    @Autowired PricingConfigRepository repository;

    private OrderModuleConfig create(List<StoreType> stores) {
        return modules.save(null,"test-module","test","test","test","test","description","description","description","description","description",OrderType.DOWNLOAD,new BigDecimal("2.5"),new BigDecimal("3"),true,999,stores);
    }
    private OrderTypeRegionPricingResult config(Long id,List<String> regions,Map<String,BigDecimal> prices) {
        return new OrderTypeRegionPricingResult(id,OrderType.DOWNLOAD,regions,Map.of(PriceCode.DOWNLOAD,prices));
    }
    @Test void regionErrorHasLocalizedMessageWithDefaultFallback() {
        assertThat(messages.getMessage("error.STORE_REGION_NOT_SUPPORTED", null, Locale.SIMPLIFIED_CHINESE))
                .contains("所选地区").doesNotContain("STORE_REGION_NOT_SUPPORTED");
        assertThat(messages.getMessage("error.STORE_REGION_NOT_SUPPORTED", null, Locale.ENGLISH))
                .contains("region");
    }
    @Test void staleChinaSelectionIsRejectedForGooglePlayButCleanedSelectionSaves() {
        var module = create(List.of(StoreType.GOOGLE_PLAY));
        repository.replaceModuleRegions(module.id(), List.of("CN", "US"));
        assertThatThrownBy(() -> pricing.updateOrderTypeRegionPricing(List.of(config(module.id(), List.of("CN", "US"), Map.of()))))
                .isInstanceOf(BusinessException.class);
        pricing.updateOrderTypeRegionPricing(List.of(config(module.id(), List.of("US"), Map.of())));
        assertThat(repository.findModuleRegions(module.id())).containsExactly("US");
    }
    @Test void sameTypeModulesHaveIndependentStoresCountriesAndPrices() {
        var a=create(List.of(StoreType.APP_STORE));
        var b=create(List.of(StoreType.GOOGLE_PLAY,StoreType.IPAD_STORE));
        assertThat(moduleRepository.findById(a.id()).orElseThrow().storeTypes()).containsExactly(StoreType.APP_STORE);
        assertThat(moduleRepository.findById(b.id()).orElseThrow().storeTypes()).containsExactly(StoreType.GOOGLE_PLAY,StoreType.IPAD_STORE);
        pricing.updateOrderTypeRegionPricing(List.of(config(a.id(),List.of("US"),Map.of("US",new BigDecimal("1.25"))),config(b.id(),List.of("US","JP"),Map.of("US",new BigDecimal("4.75")))));
        assertThat(repository.findModulePrices(a.id(),PriceCode.DOWNLOAD).get("US")).isEqualByComparingTo("1.25");
        assertThat(repository.findModulePrices(b.id(),PriceCode.DOWNLOAD).get("US")).isEqualByComparingTo("4.75");
        pricing.updateOrderTypeRegionPricing(List.of(config(a.id(),List.of(),Map.of())));
        assertThat(repository.findModuleRegions(a.id())).isEmpty();
        assertThat(repository.findModuleRegions(b.id())).containsExactly("JP","US");
        assertThat(repository.findModulePrices(b.id(),PriceCode.DOWNLOAD)).hasSize(1);
        modules.delete(a.id());
        assertThat(moduleRepository.findById(b.id())).isPresent();
    }
    @Test void rejectsInvalidRegionsPricesTypesAndStoresBeforeWriting() {
        assertThatThrownBy(() -> create(List.of())).isInstanceOf(BusinessException.class);
        var a=create(List.of(StoreType.APP_STORE));
        assertThatThrownBy(() -> pricing.updateOrderTypeRegionPricing(List.of(config(a.id(),List.of("NOT-A-REGION"),Map.of())))).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> pricing.updateOrderTypeRegionPricing(List.of(config(a.id(),List.of("US"),Map.of("US",new BigDecimal("-1")))))).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> pricing.updateOrderTypeRegionPricing(List.of(new OrderTypeRegionPricingResult(a.id(),OrderType.REVIEW,List.of("US"),Map.of())))).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> pricing.updateOrderTypeRegionPricing(List.of(config(a.id(),List.of("US"),Map.of("US",BigDecimal.ZERO))))).isInstanceOf(BusinessException.class);
        assertThat(repository.findModuleRegions(a.id())).isEmpty();
    }
    @Test void clearingOverrideRestoresDefaultAndDeletingModuleCleansConfiguration() {
        var a=create(List.of(StoreType.APP_STORE));
        pricing.updateOrderTypeRegionPricing(List.of(config(a.id(),List.of("US"),Map.of("US",new BigDecimal("0.0001")))));
        assertThat(repository.findModulePrices(a.id(),PriceCode.DOWNLOAD).get("US")).isEqualByComparingTo("0.0001");
        pricing.updateOrderTypeRegionPricing(List.of(config(a.id(),List.of("US"),Map.of())));
        assertThat(repository.findModulePrices(a.id(),PriceCode.DOWNLOAD)).isEmpty();
        assertThat(moduleRepository.findById(a.id()).orElseThrow().priceFor("US")).isEqualByComparingTo("2.5");
        modules.delete(a.id());
        assertThat(repository.findModuleRegions(a.id())).isEmpty();
        assertThat(repository.findModulePrices(a.id(),PriceCode.DOWNLOAD)).isEmpty();
    }
}
