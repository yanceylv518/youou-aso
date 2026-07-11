package com.youou.aso.modules.pricing.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.pricing.dto.PricingConfigResult;
import com.youou.aso.modules.pricing.service.PricingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer/pricing")
public class CustomerPricingController {
    private final PricingService pricingService;

    public CustomerPricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping
    public ApiResponse<List<PricingConfigResult>> listPricing(@AuthenticationPrincipal AuthenticatedAccount account) {
        requireCustomer(account);
        return ApiResponse.ok(pricingService.listPricingResults());
    }

    private void requireCustomer(AuthenticatedAccount account) {
        if (account == null || !AccountType.CUSTOMER.name().equals(account.accountType())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
