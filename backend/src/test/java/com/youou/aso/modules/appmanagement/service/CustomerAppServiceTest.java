package com.youou.aso.modules.appmanagement.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.appmanagement.domain.CustomerApp;
import com.youou.aso.modules.appmanagement.domain.CustomerAppStatus;
import com.youou.aso.modules.appmanagement.domain.MarketRegion;
import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.appmanagement.dto.CreateCustomerAppCommand;
import com.youou.aso.modules.appmanagement.dto.CreateManualCustomerAppCommand;
import com.youou.aso.modules.appmanagement.dto.CustomerAppQuery;
import com.youou.aso.modules.appmanagement.dto.SearchStoreAppCommand;
import com.youou.aso.modules.appmanagement.repository.CustomerAppRepository;
import com.youou.aso.modules.appmanagement.repository.MarketRegionRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerAppServiceTest {
    private final InMemoryCustomerAppRepository repository = new InMemoryCustomerAppRepository();
    private final InMemoryMarketRegionRepository regionRepository = new InMemoryMarketRegionRepository();
    private final FakeAppStoreVerifier verifier = new FakeAppStoreVerifier();
    private final CustomerAppService service = new CustomerAppService(repository, regionRepository, verifier);

    @Test
    void createCustomerAppSavesVerifiedAppMetadata() {
        verifier.addVerified(new VerifiedStoreApp(
                StoreType.APP_STORE,
                "US",
                "123456789",
                "Youou Demo",
                "https://example.com/icon.png",
                "com.youou.demo",
                "123456789",
                "Business"
        ));

        var result = service.create(10L, new CreateCustomerAppCommand(
                StoreType.APP_STORE,
                "us",
                "123456789"
        ));

        CustomerApp saved = repository.findById(result.id()).orElseThrow();
        assertThat(saved.getCustomerId()).isEqualTo(10L);
        assertThat(saved.getStoreType()).isEqualTo(StoreType.APP_STORE);
        assertThat(saved.getRegionCode()).isEqualTo("US");
        assertThat(saved.getAppIdentifier()).isEqualTo("123456789");
        assertThat(saved.getAppName()).isEqualTo("Youou Demo");
        assertThat(saved.getAppIconUrl()).isEqualTo("https://example.com/icon.png");
        assertThat(saved.getBundleId()).isEqualTo("com.youou.demo");
        assertThat(saved.getExternalAppId()).isEqualTo("123456789");
        assertThat(saved.getCategory()).isEqualTo("Business");
        assertThat(saved.getStatus()).isEqualTo(CustomerAppStatus.ACTIVE);
    }

    @Test
    void createCustomerAppUsesCategoryHintWhenVerifiedCategoryIsEmpty() {
        verifier.addVerified(new VerifiedStoreApp(
                StoreType.GOOGLE_PLAY,
                "US",
                "com.youou.demo",
                "Youou Demo",
                "https://example.com/icon.png",
                "com.youou.demo",
                "com.youou.demo",
                null
        ));

        var result = service.create(10L, new CreateCustomerAppCommand(
                StoreType.GOOGLE_PLAY,
                "US",
                "com.youou.demo",
                "Business"
        ));

        CustomerApp saved = repository.findById(result.id()).orElseThrow();
        assertThat(saved.getCategory()).isEqualTo("Business");
    }

    @Test
    void createCustomerAppRejectsUnknownStoreApp() {
        assertThatThrownBy(() -> service.create(10L, new CreateCustomerAppCommand(
                StoreType.GOOGLE_PLAY,
                "US",
                "missing.app"
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.APP_NOT_FOUND_IN_STORE);
    }

    @Test
    void createManualCustomerAppSavesProvidedMetadataWithoutStoreVerification() {
        var result = service.createManual(10L, new CreateManualCustomerAppCommand(
                StoreType.GOOGLE_PLAY,
                "us",
                "com.youou.manual",
                "Manual Demo",
                "https://example.com/manual.png",
                "Tools"
        ));

        CustomerApp saved = repository.findById(result.id()).orElseThrow();
        assertThat(saved.getCustomerId()).isEqualTo(10L);
        assertThat(saved.getStoreType()).isEqualTo(StoreType.GOOGLE_PLAY);
        assertThat(saved.getRegionCode()).isEqualTo("US");
        assertThat(saved.getAppIdentifier()).isEqualTo("com.youou.manual");
        assertThat(saved.getAppName()).isEqualTo("Manual Demo");
        assertThat(saved.getAppIconUrl()).isEqualTo("https://example.com/manual.png");
        assertThat(saved.getBundleId()).isEqualTo("com.youou.manual");
        assertThat(saved.getExternalAppId()).isEqualTo("com.youou.manual");
        assertThat(saved.getCategory()).isEqualTo("Tools");
        assertThat(saved.getStatus()).isEqualTo(CustomerAppStatus.ACTIVE);
    }

    @Test
    void createManualCustomerAppRejectsBlankAppName() {
        assertThatThrownBy(() -> service.createManual(10L, new CreateManualCustomerAppCommand(
                StoreType.APP_STORE,
                "US",
                "123456789",
                " ",
                null,
                null
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void createCustomerAppRejectsDuplicateAppForSameCustomerStoreAndRegion() {
        verifier.addVerified(new VerifiedStoreApp(
                StoreType.IPAD_STORE,
                "US",
                "987654321",
                "Youou iPad",
                null,
                "com.youou.ipad",
                "987654321",
                "Productivity"
        ));
        service.create(10L, new CreateCustomerAppCommand(StoreType.IPAD_STORE, "US", "987654321"));

        assertThatThrownBy(() -> service.create(10L, new CreateCustomerAppCommand(
                StoreType.IPAD_STORE,
                "US",
                "987654321"
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.APP_ALREADY_EXISTS);
    }

    @Test
    void createCustomerAppAddsRegionToExistingAppForSameCustomerStoreAndIdentifier() {
        regionRepository.addRegion("CN", true, true, true, true);
        verifier.addVerified(new VerifiedStoreApp(
                StoreType.APP_STORE,
                "US",
                "123456789",
                "Youou Demo",
                "https://example.com/icon.png",
                "com.youou.demo",
                "123456789",
                "Business"
        ));
        verifier.addVerified(new VerifiedStoreApp(
                StoreType.APP_STORE,
                "CN",
                "123456789",
                "Youou Demo",
                "https://example.com/icon.png",
                "com.youou.demo",
                "123456789",
                "Business"
        ));

        var first = service.create(10L, new CreateCustomerAppCommand(StoreType.APP_STORE, "US", "123456789"));
        var second = service.create(10L, new CreateCustomerAppCommand(StoreType.APP_STORE, "CN", "123456789"));

        assertThat(second.id()).isEqualTo(first.id());
        assertThat(repository.findByCustomerId(10L, new CustomerAppQuery(null, null, null, null))).hasSize(1);
        assertThat(service.listByCustomer(10L, new CustomerAppQuery(null, null, null, null)).get(0).regionCodes())
                .containsExactly("US", "CN");
    }

    @Test
    void searchStoreAppsReturnsRealStoreCandidates() {
        verifier.addSearchResult(new StoreAppSearchResult(
                StoreType.APP_STORE,
                "US",
                "123456789",
                "Youou Demo",
                "https://example.com/icon.png",
                "com.youou.demo",
                "123456789",
                "Business",
                "Youou Inc."
        ));

        var results = service.search(new SearchStoreAppCommand(
                StoreType.APP_STORE,
                "us",
                "youou",
                10
        ));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).regionCode()).isEqualTo("US");
        assertThat(results.get(0).appName()).isEqualTo("Youou Demo");
        assertThat(results.get(0).appIconUrl()).isEqualTo("https://example.com/icon.png");
        assertThat(results.get(0).developerName()).isEqualTo("Youou Inc.");
    }

    @Test
    void createCustomerAppRejectsUnsupportedStoreRegionPair() {
        regionRepository.addRegion("CN", true, false, true, true);
        verifier.addVerified(new VerifiedStoreApp(
                StoreType.GOOGLE_PLAY,
                "CN",
                "com.youou.demo",
                "Youou Demo",
                null,
                "com.youou.demo",
                "com.youou.demo",
                "Tools"
        ));

        assertThatThrownBy(() -> service.create(10L, new CreateCustomerAppCommand(
                StoreType.GOOGLE_PLAY,
                "CN",
                "com.youou.demo"
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STORE_REGION_NOT_SUPPORTED);
    }

    @Test
    void disableCustomerAppHidesItFromActiveListAndAllowsReAdding() {
        verifier.addVerified(new VerifiedStoreApp(
                StoreType.APP_STORE,
                "US",
                "123456789",
                "Youou Demo",
                null,
                "com.youou.demo",
                "123456789",
                "Business"
        ));
        var created = service.create(10L, new CreateCustomerAppCommand(StoreType.APP_STORE, "US", "123456789"));

        service.disable(10L, created.id());

        assertThat(repository.findById(created.id()).orElseThrow().getStatus()).isEqualTo(CustomerAppStatus.DISABLED);
        assertThat(service.listByCustomer(10L, new CustomerAppQuery(null, null, null, null))).isEmpty();
        var recreated = service.create(10L, new CreateCustomerAppCommand(StoreType.APP_STORE, "US", "123456789"));
        assertThat(recreated.id()).isNotEqualTo(created.id());
    }

    private static class InMemoryCustomerAppRepository implements CustomerAppRepository {
        private final List<CustomerApp> apps = new ArrayList<>();
        private final Map<Long, List<String>> regions = new LinkedHashMap<>();
        private long nextId = 1L;

        @Override
        public Optional<CustomerApp> findById(Long id) {
            return apps.stream().filter(app -> app.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<CustomerApp> findActiveByCustomerStoreAndIdentifier(Long customerId, StoreType storeType, String appIdentifier) {
            return apps.stream().filter(app ->
                    app.getCustomerId().equals(customerId)
                            && app.getStoreType() == storeType
                            && app.getAppIdentifier().equals(appIdentifier)
                            && app.getStatus() == CustomerAppStatus.ACTIVE
            ).findFirst();
        }

        @Override
        public boolean existsByCustomerAndStoreAndRegionAndIdentifier(Long customerId, StoreType storeType, String regionCode, String appIdentifier) {
            return apps.stream().anyMatch(app ->
                    app.getCustomerId().equals(customerId)
                            && app.getStoreType() == storeType
                            && app.getRegionCode().equals(regionCode)
                            && app.getAppIdentifier().equals(appIdentifier)
                            && app.getStatus() == CustomerAppStatus.ACTIVE
            );
        }

        @Override
        public boolean existsByCustomerAndStoreAndRegionAndIdentifierExcludingId(Long customerId, StoreType storeType, String regionCode, String appIdentifier, Long excludedId) {
            return apps.stream().anyMatch(app ->
                    app.getCustomerId().equals(customerId)
                            && app.getStoreType() == storeType
                            && app.getRegionCode().equals(regionCode)
                            && app.getAppIdentifier().equals(appIdentifier)
                            && app.getStatus() == CustomerAppStatus.ACTIVE
                            && !app.getId().equals(excludedId)
            );
        }

        @Override
        public CustomerApp save(CustomerApp app) {
            app.setId(nextId++);
            apps.add(app);
            addRegion(app.getId(), app.getRegionCode());
            return app;
        }

        @Override
        public void addRegion(Long customerAppId, String regionCode) {
            regions.computeIfAbsent(customerAppId, ignored -> new ArrayList<>());
            if (!regions.get(customerAppId).contains(regionCode)) {
                regions.get(customerAppId).add(regionCode);
            }
        }

        @Override
        public boolean existsRegion(Long customerAppId, String regionCode) {
            return regions.getOrDefault(customerAppId, List.of()).contains(regionCode);
        }

        @Override
        public Map<Long, List<String>> findRegionCodesByAppIds(List<Long> appIds) {
            Map<Long, List<String>> result = new LinkedHashMap<>();
            appIds.forEach(appId -> result.put(appId, List.copyOf(regions.getOrDefault(appId, List.of()))));
            return result;
        }

        @Override
        public CustomerApp update(CustomerApp app) {
            return app;
        }

        @Override
        public void updateStatus(Long id, CustomerAppStatus status) {
            findById(id).ifPresent(app -> app.setStatus(status));
        }

        @Override
        public List<CustomerApp> findByCustomerId(Long customerId, CustomerAppQuery query) {
            return filter(query).stream().filter(app -> app.getCustomerId().equals(customerId)).toList();
        }

        @Override
        public List<CustomerApp> findAll(CustomerAppQuery query) {
            return filter(query);
        }

        private List<CustomerApp> filter(CustomerAppQuery query) {
            CustomerAppStatus status = query == null || query.status() == null ? CustomerAppStatus.ACTIVE : query.status();
            return apps.stream()
                    .filter(app -> app.getStatus() == status)
                    .filter(app -> query == null || query.storeType() == null || app.getStoreType() == query.storeType())
                    .filter(app -> query == null || query.regionCode() == null || query.regionCode().isBlank() || app.getRegionCode().equals(query.regionCode()))
                    .filter(app -> {
                        if (query == null || query.keyword() == null || query.keyword().isBlank()) {
                            return true;
                        }
                        String keyword = query.keyword();
                        return app.getAppName().contains(keyword)
                                || app.getAppIdentifier().contains(keyword)
                                || (app.getBundleId() != null && app.getBundleId().contains(keyword))
                                || (app.getExternalAppId() != null && app.getExternalAppId().contains(keyword))
                                || (app.getCategory() != null && app.getCategory().contains(keyword));
                    })
                    .toList();
        }
    }

    private static class FakeAppStoreVerifier implements AppStoreVerifier {
        private final List<VerifiedStoreApp> verifiedApps = new ArrayList<>();
        private final List<StoreAppSearchResult> searchResults = new ArrayList<>();

        void addVerified(VerifiedStoreApp app) {
            verifiedApps.add(app);
        }

        void addSearchResult(StoreAppSearchResult app) {
            searchResults.add(app);
        }

        @Override
        public Optional<VerifiedStoreApp> verify(StoreType storeType, String regionCode, String appIdentifier) {
            return verifiedApps.stream()
                    .filter(app -> app.storeType() == storeType)
                    .filter(app -> app.regionCode().equals(regionCode))
                    .filter(app -> app.appIdentifier().equals(appIdentifier))
                    .findFirst();
        }

        @Override
        public List<StoreAppSearchResult> search(StoreType storeType, String regionCode, String keyword, int limit) {
            return searchResults.stream()
                    .filter(app -> app.storeType() == storeType)
                    .filter(app -> app.regionCode().equals(regionCode))
                    .limit(limit)
                    .toList();
        }
    }

    private static class InMemoryMarketRegionRepository implements MarketRegionRepository {
        private final List<MarketRegion> regions = new ArrayList<>();

        InMemoryMarketRegionRepository() {
            addRegion("US", true, true, true, true);
        }

        void addRegion(String code, boolean supportsAppStore, boolean supportsGooglePlay, boolean supportsIpadStore, boolean enabled) {
            MarketRegion region = new MarketRegion();
            region.setCode(code);
            region.setNameZh(code);
            region.setNameEn(code);
            region.setEnabled(enabled);
            region.setSupportsAppStore(supportsAppStore);
            region.setSupportsGooglePlay(supportsGooglePlay);
            region.setSupportsIpadStore(supportsIpadStore);
            regions.removeIf(item -> item.getCode().equals(code));
            regions.add(region);
        }

        @Override
        public Optional<MarketRegion> findByCode(String code) {
            return regions.stream().filter(region -> region.getCode().equals(code)).findFirst();
        }

        @Override
        public List<MarketRegion> findAll() {
            return List.copyOf(regions);
        }

        @Override
        public List<MarketRegion> findEnabled() {
            return regions.stream().filter(MarketRegion::isEnabled).toList();
        }

        @Override
        public void update(MarketRegion region) {
            regions.removeIf(item -> item.getCode().equals(region.getCode()));
            regions.add(region);
        }
    }
}
