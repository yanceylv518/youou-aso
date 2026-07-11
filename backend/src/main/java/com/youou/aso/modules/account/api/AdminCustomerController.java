package com.youou.aso.modules.account.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.common.api.PageResult;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountStatus;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.dto.CustomerAccountResult;
import com.youou.aso.modules.account.service.AdminCustomerService;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/customers")
public class AdminCustomerController {
    private final AdminCustomerService adminCustomerService;

    public AdminCustomerController(AdminCustomerService adminCustomerService) {
        this.adminCustomerService = adminCustomerService;
    }

    @PreAuthorize("@perm.hasMenu('customers')")
    @GetMapping
    public ApiResponse<?> list(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        ensureAdmin(account);
        if (page != null || pageSize != null) {
            PageResult<CustomerAccountResult> result = adminCustomerService.pageCustomers(keyword, page, pageSize);
            return ApiResponse.ok(result);
        }
        return ApiResponse.ok(adminCustomerService.listCustomers(keyword));
    }

    public ApiResponse<List<CustomerAccountResult>> list(AuthenticatedAccount account, String keyword) {
        ensureAdmin(account);
        return ApiResponse.ok(adminCustomerService.listCustomers(keyword));
    }

    @PreAuthorize("@perm.has('customer:status')")
    @PutMapping("/{id}/status")
    public ApiResponse<CustomerAccountResult> updateStatus(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id,
            @RequestBody UpdateCustomerStatusRequest request
    ) {
        ensureAdmin(account);
        return ApiResponse.ok(adminCustomerService.updateStatus(id, request.status()));
    }

    private void ensureAdmin(AuthenticatedAccount account) {
        if (account == null || !AccountType.ADMIN.name().equals(account.accountType())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    public record UpdateCustomerStatusRequest(AccountStatus status) {
    }
}
