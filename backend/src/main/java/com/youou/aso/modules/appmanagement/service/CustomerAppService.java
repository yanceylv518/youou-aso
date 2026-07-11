package com.youou.aso.modules.appmanagement.service;

import com.youou.aso.common.api.PageResult;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.appmanagement.domain.CustomerApp;
import com.youou.aso.modules.appmanagement.domain.CustomerAppStatus;
import com.youou.aso.modules.appmanagement.domain.MarketRegion;
import com.youou.aso.modules.appmanagement.dto.CreateCustomerAppCommand;
import com.youou.aso.modules.appmanagement.dto.CreateManualCustomerAppCommand;
import com.youou.aso.modules.appmanagement.dto.CustomerAppQuery;
import com.youou.aso.modules.appmanagement.dto.CustomerAppResult;
import com.youou.aso.modules.appmanagement.dto.SearchStoreAppCommand;
import com.youou.aso.modules.appmanagement.repository.CustomerAppRepository;
import com.youou.aso.modules.appmanagement.repository.MarketRegionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CustomerAppService {
    private final CustomerAppRepository customerAppRepository;
    private final MarketRegionRepository marketRegionRepository;
    private final AppStoreVerifier appStoreVerifier;
    private final AppIconStorageService appIconStorageService;

    public CustomerAppService(
            CustomerAppRepository customerAppRepository,
            MarketRegionRepository marketRegionRepository,
            AppStoreVerifier appStoreVerifier
    ) {
        this(customerAppRepository, marketRegionRepository, appStoreVerifier, null);
    }

    @Autowired
    public CustomerAppService(
            CustomerAppRepository customerAppRepository,
            MarketRegionRepository marketRegionRepository,
            AppStoreVerifier appStoreVerifier,
            AppIconStorageService appIconStorageService
    ) {
        this.customerAppRepository = customerAppRepository;
        this.marketRegionRepository = marketRegionRepository;
        this.appStoreVerifier = appStoreVerifier;
        this.appIconStorageService = appIconStorageService;
    }

    public CustomerAppResult create(Long customerId, CreateCustomerAppCommand command) {
        String regionCode = normalizeRegionCode(command.regionCode());
        String appIdentifier = normalizeAppIdentifier(command.appIdentifier());
        MarketRegion region = requireSupportedRegion(regionCode, command.storeType());

        VerifiedStoreApp verifiedApp = appStoreVerifier.verify(command.storeType(), region.getCode(), appIdentifier)
                .orElseThrow(() -> new BusinessException(ErrorCode.APP_NOT_FOUND_IN_STORE));

        CustomerApp existing = customerAppRepository.findActiveByCustomerStoreAndIdentifier(
                customerId,
                command.storeType(),
                appIdentifier
        ).orElse(null);
        if (existing != null) {
            if (customerAppRepository.existsRegion(existing.getId(), region.getCode())) {
                throw new BusinessException(ErrorCode.APP_ALREADY_EXISTS);
            }
            customerAppRepository.addRegion(existing.getId(), region.getCode());
            return toResult(existing, List.of(region.getCode()));
        }

        CustomerApp app = new CustomerApp();
        app.setCustomerId(customerId);
        app.setStoreType(command.storeType());
        app.setRegionCode(region.getCode());
        app.setAppIdentifier(appIdentifier);
        app.setAppName(verifiedApp.appName());
        app.setAppIconUrl(localizeAppIconUrl(verifiedApp.appIconUrl()));
        app.setBundleId(verifiedApp.bundleId());
        app.setExternalAppId(verifiedApp.externalAppId());
        app.setCategory(resolveCategory(verifiedApp.category(), command.categoryHint()));
        app.setStatus(CustomerAppStatus.ACTIVE);
        app.setVerifiedAt(LocalDateTime.now());
        CustomerApp saved = customerAppRepository.save(app);
        return toResult(saved, List.of(region.getCode()));
    }

    public CustomerAppResult createManual(Long customerId, CreateManualCustomerAppCommand command) {
        if (customerId == null || command == null || command.storeType() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        String regionCode = normalizeRegionCode(command.regionCode());
        String appIdentifier = normalizeAppIdentifier(command.appIdentifier());
        String appName = normalizeRequiredText(command.appName(), 255);
        String appIconUrl = localizeAppIconUrl(command.appIconUrl());
        String category = normalizeNullableText(command.categoryHint(), 128);
        MarketRegion region = requireSupportedRegion(regionCode, command.storeType());

        CustomerApp existing = customerAppRepository.findActiveByCustomerStoreAndIdentifier(
                customerId,
                command.storeType(),
                appIdentifier
        ).orElse(null);
        if (existing != null) {
            if (customerAppRepository.existsRegion(existing.getId(), region.getCode())) {
                throw new BusinessException(ErrorCode.APP_ALREADY_EXISTS);
            }
            customerAppRepository.addRegion(existing.getId(), region.getCode());
            return toResult(existing, List.of(region.getCode()));
        }

        CustomerApp app = new CustomerApp();
        app.setCustomerId(customerId);
        app.setStoreType(command.storeType());
        app.setRegionCode(region.getCode());
        app.setAppIdentifier(appIdentifier);
        app.setAppName(appName);
        app.setAppIconUrl(appIconUrl);
        app.setBundleId(resolveManualBundleId(command.storeType(), appIdentifier));
        app.setExternalAppId(resolveManualExternalAppId(command.storeType(), appIdentifier));
        app.setCategory(category);
        app.setStatus(CustomerAppStatus.ACTIVE);
        app.setVerifiedAt(LocalDateTime.now());
        CustomerApp saved = customerAppRepository.save(app);
        return toResult(saved, List.of(region.getCode()));
    }

    public void disable(Long customerId, Long appId) {
        CustomerApp app = customerAppRepository.findById(appId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        ensureOwnActiveApp(app, customerId);
        customerAppRepository.updateStatus(appId, CustomerAppStatus.DISABLED);
    }

    public void disableByAdmin(Long appId) {
        CustomerApp app = customerAppRepository.findById(appId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (app.getStatus() != CustomerAppStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        customerAppRepository.updateStatus(appId, CustomerAppStatus.DISABLED);
    }

    public List<StoreAppSearchResult> search(SearchStoreAppCommand command) {
        String regionCode = normalizeRegionCode(command.regionCode());
        String keyword = normalizeKeyword(command.keyword());
        if (keyword.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        MarketRegion region = marketRegionRepository.findByCode(regionCode)
                .filter(MarketRegion::isEnabled)
                .orElseThrow(() -> new BusinessException(ErrorCode.REGION_DISABLED));
        if (!region.supports(command.storeType())) {
            throw new BusinessException(ErrorCode.STORE_REGION_NOT_SUPPORTED);
        }
        int limit = Math.max(1, Math.min(command.limit(), 20));
        return appStoreVerifier.search(command.storeType(), region.getCode(), keyword, limit);
    }

    public List<CustomerAppResult> listByCustomer(Long customerId, CustomerAppQuery query) {
        List<CustomerApp> apps = customerAppRepository.findByCustomerId(customerId, activeQuery(query));
        Map<Long, List<String>> regionMap = customerAppRepository.findRegionCodesByAppIds(apps.stream().map(CustomerApp::getId).toList());
        return apps.stream()
                .map(app -> toResult(app, regionMap.getOrDefault(app.getId(), List.of(app.getRegionCode()))))
                .toList();
    }

    public PageResult<CustomerAppResult> pageByCustomer(Long customerId, CustomerAppQuery query, Integer page, Integer pageSize) {
        Page normalized = normalizePage(page, pageSize);
        CustomerAppQuery activeQuery = activeQuery(query);
        List<CustomerApp> apps = customerAppRepository.findByCustomerId(customerId, activeQuery, normalized.pageSize(), normalized.offset());
        long total = customerAppRepository.countByCustomerId(customerId, activeQuery);
        return new PageResult<>(toResults(apps), normalized.page(), normalized.pageSize(), total);
    }

    public List<CustomerAppResult> listAll(CustomerAppQuery query) {
        List<CustomerApp> apps = customerAppRepository.findAll(activeQuery(query));
        return toResults(apps);
    }

    public PageResult<CustomerAppResult> pageAll(CustomerAppQuery query, Integer page, Integer pageSize) {
        Page normalized = normalizePage(page, pageSize);
        CustomerAppQuery activeQuery = activeQuery(query);
        List<CustomerApp> apps = customerAppRepository.findAll(activeQuery, normalized.pageSize(), normalized.offset());
        long total = customerAppRepository.countAll(activeQuery);
        return new PageResult<>(toResults(apps), normalized.page(), normalized.pageSize(), total);
    }

    private List<CustomerAppResult> toResults(List<CustomerApp> apps) {
        Map<Long, List<String>> regionMap = customerAppRepository.findRegionCodesByAppIds(apps.stream().map(CustomerApp::getId).toList());
        return apps.stream()
                .map(app -> toResult(app, regionMap.getOrDefault(app.getId(), List.of(app.getRegionCode()))))
                .toList();
    }

    private Page normalizePage(Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        return new Page(safePage, safePageSize);
    }

    private record Page(int page, int pageSize) {
        int offset() {
            return (page - 1) * pageSize;
        }
    }

    private CustomerAppQuery activeQuery(CustomerAppQuery query) {
        if (query == null) {
            return new CustomerAppQuery(null, null, null, CustomerAppStatus.ACTIVE);
        }
        return new CustomerAppQuery(
                normalizeKeyword(query.keyword()),
                query.storeType(),
                normalizeRegionCode(query.regionCode()),
                CustomerAppStatus.ACTIVE
        );
    }

    private MarketRegion requireSupportedRegion(String regionCode, com.youou.aso.modules.appmanagement.domain.StoreType storeType) {
        MarketRegion region = marketRegionRepository.findByCode(regionCode)
                .filter(MarketRegion::isEnabled)
                .orElseThrow(() -> new BusinessException(ErrorCode.REGION_DISABLED));
        if (!region.supports(storeType)) {
            throw new BusinessException(ErrorCode.STORE_REGION_NOT_SUPPORTED);
        }
        return region;
    }

    private void ensureOwnActiveApp(CustomerApp app, Long customerId) {
        if (!app.getCustomerId().equals(customerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (app.getStatus() != CustomerAppStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
    }

    private CustomerAppResult toResult(CustomerApp app, List<String> regionCodes) {
        return new CustomerAppResult(
                app.getId(),
                app.getCustomerId(),
                app.getStoreType(),
                app.getRegionCode(),
                app.getAppIdentifier(),
                app.getAppName(),
                app.getAppIconUrl(),
                app.getBundleId(),
                app.getExternalAppId(),
                app.getCategory(),
                regionCodes,
                app.getCustomerUsername(),
                app.getCustomerEmail(),
                app.getStatus().name(),
                app.getVerifiedAt(),
                app.getCreatedAt()
        );
    }

    private String normalizeRegionCode(String regionCode) {
        return regionCode == null ? "" : regionCode.trim().toUpperCase();
    }

    private String normalizeAppIdentifier(String appIdentifier) {
        String normalized = appIdentifier == null ? "" : appIdentifier.trim();
        if (normalized.isBlank() || normalized.length() > 255) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        return normalized;
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    private String localizeAppIconUrl(String appIconUrl) {
        String normalized = normalizeNullableText(appIconUrl, 500);
        if (normalized == null || normalized.startsWith(AppIconStorageService.PUBLIC_PATH_PREFIX)) {
            return normalized;
        }
        if (appIconStorageService == null) {
            return normalized;
        }
        return appIconStorageService.storeRemote(normalized).orElse(null);
    }

    private String resolveCategory(String verifiedCategory, String categoryHint) {
        String normalizedVerifiedCategory = normalizeNullableText(verifiedCategory, 128);
        if (normalizedVerifiedCategory != null) {
            return normalizedVerifiedCategory;
        }
        return normalizeNullableText(categoryHint, 128);
    }

    private String normalizeNullableText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isBlank()) {
            return null;
        }
        return normalized.length() > maxLength ? normalized.substring(0, maxLength) : normalized;
    }

    private String normalizeRequiredText(String value, int maxLength) {
        String normalized = normalizeNullableText(value, maxLength);
        if (normalized == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        return normalized;
    }

    private String resolveManualBundleId(com.youou.aso.modules.appmanagement.domain.StoreType storeType, String appIdentifier) {
        return storeType == com.youou.aso.modules.appmanagement.domain.StoreType.GOOGLE_PLAY ? appIdentifier : null;
    }

    private String resolveManualExternalAppId(com.youou.aso.modules.appmanagement.domain.StoreType storeType, String appIdentifier) {
        return storeType == com.youou.aso.modules.appmanagement.domain.StoreType.GOOGLE_PLAY ? appIdentifier : appIdentifier;
    }
}
