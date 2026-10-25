package com.youou.aso.modules.account.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.service.AdminReferralService;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/referral")
public class AdminReferralController {
    private final AdminReferralService service;
    public AdminReferralController(AdminReferralService service) { this.service = service; }
    @GetMapping
    public ApiResponse<String> get(@AuthenticationPrincipal AuthenticatedAccount account) {
        if (account == null || !"ADMIN".equals(account.accountType())) throw new BusinessException(ErrorCode.FORBIDDEN);
        return ApiResponse.ok(service.registrationPath(account.accountId()));
    }
}
