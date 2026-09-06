package com.youou.aso.modules.pricing.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.pricing.domain.PriceCode;
import com.youou.aso.modules.pricing.dto.PricingConfigResult;
import com.youou.aso.modules.pricing.dto.UpdatePricingCommand;
import com.youou.aso.modules.pricing.dto.OrderTypeRegionPricingResult;
import com.youou.aso.modules.pricing.service.PricingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/pricing")
public class AdminPricingController {
    private final PricingService pricingService;

    public AdminPricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @PreAuthorize("@perm.hasMenu('system.pricing')")
    @GetMapping
    public ApiResponse<List<PricingConfigResult>> listPricing(@AuthenticationPrincipal AuthenticatedAccount account) {
        requireAdmin(account);
        return ApiResponse.ok(pricingService.listPricingResults());
    }

    @PreAuthorize("@perm.has('pricing:update')")
    @PutMapping
    public ApiResponse<List<PricingConfigResult>> updatePricing(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @Valid @RequestBody UpdatePricingRequest request
    ) {
        requireAdmin(account);
        return ApiResponse.ok(pricingService.updatePricing(request.items().stream()
                .map(item -> new UpdatePricingCommand(item.code(), item.unitPrice(), item.chinaUnitPrice()))
                .toList()));
    }

    @PreAuthorize("@perm.hasMenu('system.pricing')")
    @GetMapping("/regions")
    public ApiResponse<List<OrderTypeRegionPricingResult>> listRegionPricing(@AuthenticationPrincipal AuthenticatedAccount account) {
        requireAdmin(account);
        return ApiResponse.ok(pricingService.listOrderTypeRegionPricing());
    }

    @PreAuthorize("@perm.has('pricing:update')")
    @PutMapping("/regions")
    public ApiResponse<List<OrderTypeRegionPricingResult>> updateRegionPricing(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @RequestBody List<OrderTypeRegionPricingResult> configs
    ) {
        requireAdmin(account);
        return ApiResponse.ok(pricingService.updateOrderTypeRegionPricing(configs));
    }

    private void requireAdmin(AuthenticatedAccount account) {
        if (account == null || !AccountType.ADMIN.name().equals(account.accountType())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    public record UpdatePricingRequest(
            @NotEmpty
            List<@Valid PriceItemRequest> items
    ) {
    }

    public record PriceItemRequest(
            @NotNull
            PriceCode code,

            @NotNull
            @DecimalMin(value = "0.00")
            BigDecimal unitPrice,

            @NotNull
            @DecimalMin(value = "0.00")
            BigDecimal chinaUnitPrice
    ) {
    }
}
