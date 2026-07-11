package com.youou.aso.modules.account.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.modules.account.dto.AdminMenuResult;
import com.youou.aso.modules.account.dto.AdminRoleResult;
import com.youou.aso.modules.account.dto.SaveAdminRoleCommand;
import com.youou.aso.modules.account.service.AdminPermissionService;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import jakarta.validation.Valid;
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
@RequestMapping("/api/admin/roles")
public class AdminRoleController {
    private final AdminPermissionService adminPermissionService;

    public AdminRoleController(AdminPermissionService adminPermissionService) {
        this.adminPermissionService = adminPermissionService;
    }

    @PreAuthorize("@perm.hasMenu('system.roles')")
    @GetMapping("/menus")
    public ApiResponse<List<AdminMenuResult>> menus(@AuthenticationPrincipal AuthenticatedAccount account) {
        adminPermissionService.ensureSuperAdmin(account);
        return ApiResponse.ok(adminPermissionService.listMenus());
    }

    @PreAuthorize("@perm.hasMenu('system.roles')")
    @GetMapping
    public ApiResponse<List<AdminRoleResult>> list(@AuthenticationPrincipal AuthenticatedAccount account) {
        adminPermissionService.ensureSuperAdmin(account);
        return ApiResponse.ok(adminPermissionService.listRoles());
    }

    @PreAuthorize("@perm.has('role:manage')")
    @PostMapping
    public ApiResponse<AdminRoleResult> create(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @Valid @RequestBody SaveAdminRoleRequest request
    ) {
        adminPermissionService.ensureSuperAdmin(account);
        return ApiResponse.ok(adminPermissionService.createRole(request.toCommand()));
    }

    @PreAuthorize("@perm.has('role:manage')")
    @PutMapping("/{id}")
    public ApiResponse<AdminRoleResult> update(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id,
            @Valid @RequestBody UpdateAdminRoleRequest request
    ) {
        adminPermissionService.ensureSuperAdmin(account);
        return ApiResponse.ok(adminPermissionService.updateRole(id, request.toCommand()));
    }

    public record SaveAdminRoleRequest(
            @NotBlank
            @Size(min = 2, max = 32)
            @Pattern(regexp = "^[A-Za-z0-9_]+$")
            String roleKey,

            @NotBlank
            @Size(max = 64)
            String roleName,

            String status,

            Integer sortOrder,

            @Size(max = 255)
            String remark,

            List<Long> menuIds
    ) {
        SaveAdminRoleCommand toCommand() {
            return new SaveAdminRoleCommand(roleKey, roleName, status, sortOrder, remark, menuIds);
        }
    }

    public record UpdateAdminRoleRequest(
            @NotBlank
            @Size(max = 64)
            String roleName,

            String status,

            Integer sortOrder,

            @Size(max = 255)
            String remark,

            List<Long> menuIds
    ) {
        SaveAdminRoleCommand toCommand() {
            return new SaveAdminRoleCommand(null, roleName, status, sortOrder, remark, menuIds);
        }
    }
}
