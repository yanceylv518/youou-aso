package com.youou.aso.modules.support.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.support.dto.HomeMetricsResult;
import com.youou.aso.modules.support.dto.UpdateHomeMetricsCommand;
import com.youou.aso.modules.support.service.HomeMetricsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HomeMetricsController {
    private final HomeMetricsService service;

    public HomeMetricsController(HomeMetricsService service) {
        this.service = service;
    }

    @GetMapping("/public/home-metrics")
    public ApiResponse<HomeMetricsResult> getPublicConfig() {
        return ApiResponse.ok(service.getConfig());
    }

    @PreAuthorize("@perm.hasMenu('system.homeMetrics')")
    @GetMapping("/admin/support/home-metrics")
    public ApiResponse<HomeMetricsResult> getAdminConfig(
            @AuthenticationPrincipal AuthenticatedAccount account
    ) {
        requireAdmin(account);
        return ApiResponse.ok(service.getConfig());
    }

    @PreAuthorize("@perm.has('homeMetrics:update')")
    @PutMapping("/admin/support/home-metrics")
    public ApiResponse<HomeMetricsResult> updateAdminConfig(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @Valid @RequestBody HomeMetricsRequest request
    ) {
        requireAdmin(account);
        return ApiResponse.ok(service.updateConfig(new UpdateHomeMetricsCommand(
                request.appsValue(),
                request.satisfactionValue(),
                request.experienceYears(),
                request.teamValue()
        )));
    }

    private void requireAdmin(AuthenticatedAccount account) {
        if (account == null || !AccountType.ADMIN.name().equals(account.accountType())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    public record HomeMetricsRequest(
            @NotBlank @Size(max = 32) String appsValue,
            @NotBlank @Size(max = 32) String satisfactionValue,
            @Min(0) @Max(999) int experienceYears,
            @NotBlank @Size(max = 32) String teamValue
    ) {
    }
}
