package com.youou.aso.modules.account.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountStatus;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.domain.AdminRole;
import com.youou.aso.modules.account.dto.AdminAccountResult;
import com.youou.aso.modules.account.dto.CreateAdminAccountCommand;
import com.youou.aso.modules.account.service.AdminAccountService;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/admin-accounts")
public class AdminAccountController {
    private final AdminAccountService adminAccountService;

    public AdminAccountController(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    @PreAuthorize("@perm.hasMenu('system.adminAccounts')")
    @GetMapping
    public ApiResponse<List<AdminAccountResult>> list(@AuthenticationPrincipal AuthenticatedAccount account) {
        ensureSuperAdmin(account);
        return ApiResponse.ok(adminAccountService.listAdmins());
    }

    @PreAuthorize("@perm.has('admin:manage')")
    @PostMapping
    public ApiResponse<AdminAccountResult> create(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @Valid @RequestBody CreateAdminRequest request
    ) {
        ensureSuperAdmin(account);
        return ApiResponse.ok(adminAccountService.createAdmin(new CreateAdminAccountCommand(
                request.username(),
                request.email(),
                request.password(),
                request.roleIds()
        )));
    }

    @PreAuthorize("@perm.has('admin:manage')")
    @PutMapping("/{id}/roles")
    public ApiResponse<AdminAccountResult> updateRoles(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id,
            @RequestBody UpdateAdminRolesRequest request
    ) {
        ensureSuperAdmin(account);
        return ApiResponse.ok(adminAccountService.updateRoles(id, request.roleIds()));
    }

    @PreAuthorize("@perm.has('admin:manage')")
    @GetMapping("/{id}/permissions")
    public ApiResponse<List<Long>> getPermissions(@AuthenticationPrincipal AuthenticatedAccount account, @PathVariable Long id) {
        ensureSuperAdmin(account);
        return ApiResponse.ok(adminAccountService.getPermissions(id));
    }

    @PreAuthorize("@perm.has('admin:manage')")
    @PutMapping("/{id}/permissions")
    public ApiResponse<List<Long>> updatePermissions(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id,
            @RequestBody UpdateAdminPermissionsRequest request
    ) {
        ensureSuperAdmin(account);
        return ApiResponse.ok(adminAccountService.updatePermissions(id, request.menuIds()));
    }

    @PreAuthorize("@perm.has('admin:manage')")
    @PutMapping("/{id}/status")
    public ApiResponse<AdminAccountResult> updateStatus(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id,
            @RequestBody UpdateAdminStatusRequest request
    ) {
        ensureSuperAdmin(account);
        return ApiResponse.ok(adminAccountService.updateStatus(account.accountId(), id, request.status()));
    }

    private void ensureSuperAdmin(AuthenticatedAccount account) {
        if (account == null
                || !AccountType.ADMIN.name().equals(account.accountType())
                || !AdminRole.SUPER_ADMIN.name().equals(account.roleCode())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    public record UpdateAdminStatusRequest(AccountStatus status) {
    }

    public record UpdateAdminRolesRequest(List<Long> roleIds) {
    }

    public record UpdateAdminPermissionsRequest(List<Long> menuIds) {
    }

    public record CreateAdminRequest(
            @NotBlank
            @Size(min = 3, max = 64)
            @Pattern(regexp = "^[A-Za-z0-9_]+$")
            String username,

            @NotBlank
            @Email
            @Size(max = 128)
            String email,

            @NotBlank
            @Size(min = 8, max = 72)
            String password,

            List<Long> roleIds
    ) {
        public CreateAdminRequest(String username, String email, String password) {
            this(username, email, password, List.of());
        }
    }
}
