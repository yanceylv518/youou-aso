package com.youou.aso.modules.appmanagement.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.appmanagement.dto.AdminMarketRegionResult;
import com.youou.aso.modules.appmanagement.service.AdminMarketRegionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/regions")
public class AdminMarketRegionController {
    private final AdminMarketRegionService adminMarketRegionService;

    public AdminMarketRegionController(AdminMarketRegionService adminMarketRegionService) {
        this.adminMarketRegionService = adminMarketRegionService;
    }

    @PreAuthorize("@perm.hasMenu('system.regions')")
    @GetMapping
    public ApiResponse<List<AdminMarketRegionResult>> list(@AuthenticationPrincipal AuthenticatedAccount account) {
        ensureAdmin(account);
        return ApiResponse.ok(adminMarketRegionService.listRegions());
    }

    @PreAuthorize("@perm.has('region:update')")
    @PostMapping
    public ApiResponse<AdminMarketRegionResult> create(@AuthenticationPrincipal AuthenticatedAccount account,
            @RequestBody SaveMarketRegionRequest request) {
        ensureAdmin(account);
        return ApiResponse.ok(adminMarketRegionService.createRegion(request.code(), request.nameZh(), request.nameEn(), request.nameRu(), request.namePt(), request.nameEs(),
                request.enabled(), request.supportsAppStore(), request.supportsGooglePlay(),
                request.supportsIpadStore(), request.sortOrder()));
    }

    @PreAuthorize("@perm.has('region:update')")
    @PutMapping("/{code}")
    public ApiResponse<AdminMarketRegionResult> update(@AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable String code, @RequestBody SaveMarketRegionRequest request) {
        ensureAdmin(account);
        return ApiResponse.ok(adminMarketRegionService.updateRegion(code, request.code(), request.nameZh(), request.nameEn(), request.nameRu(), request.namePt(), request.nameEs(),
                request.enabled(), request.supportsAppStore(), request.supportsGooglePlay(),
                request.supportsIpadStore(), request.sortOrder()));
    }

    private void ensureAdmin(AuthenticatedAccount account) {
        if (account == null || !AccountType.ADMIN.name().equals(account.accountType())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    public record SaveMarketRegionRequest(String code, String nameZh, String nameEn, String nameRu, String namePt, String nameEs, boolean enabled,
            boolean supportsAppStore, boolean supportsGooglePlay, boolean supportsIpadStore, int sortOrder) {
    }
}
