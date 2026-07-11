package com.youou.aso.modules.account.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.domain.AdminRole;
import com.youou.aso.modules.account.dto.AdminAccessResult;
import com.youou.aso.modules.account.dto.AdminMenuResult;
import com.youou.aso.modules.account.dto.AdminRoleResult;
import com.youou.aso.modules.account.dto.SaveAdminRoleCommand;
import com.youou.aso.modules.account.repository.AdminPermissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class AdminPermissionService {
    private final AdminPermissionRepository adminPermissionRepository;

    public AdminPermissionService(AdminPermissionRepository adminPermissionRepository) {
        this.adminPermissionRepository = adminPermissionRepository;
    }

    public List<AdminMenuResult> listMenus() {
        return adminPermissionRepository.findAllMenus();
    }

    public List<AdminRoleResult> listRoles() {
        return adminPermissionRepository.findAllRoles();
    }

    public AdminRoleResult createRole(SaveAdminRoleCommand command) {
        SaveAdminRoleCommand normalized = normalize(command, true);
        if (adminPermissionRepository.findRoleByKey(normalized.roleKey()).isPresent()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        return adminPermissionRepository.createRole(normalized);
    }

    public AdminRoleResult updateRole(Long id, SaveAdminRoleCommand command) {
        AdminRoleResult existing = adminPermissionRepository.findRoleById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        SaveAdminRoleCommand normalized = normalize(command, false);
        adminPermissionRepository.updateRole(existing.id(), normalized);
        return adminPermissionRepository.findRoleById(existing.id()).orElseThrow();
    }

    public List<Long> findRoleIdsByAdminId(Long adminId) {
        return adminPermissionRepository.findRoleIdsByAdminId(adminId);
    }

    public void replaceAdminRoles(Long adminId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            AdminRoleResult operator = adminPermissionRepository.findRoleByKey("OPERATOR")
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
            adminPermissionRepository.replaceAdminRoles(adminId, List.of(operator.id()));
            return;
        }
        adminPermissionRepository.replaceAdminRoles(adminId, roleIds);
    }

    public AdminAccessResult accessForAdmin(Long adminId, String roleCode) {
        if (AdminRole.SUPER_ADMIN.name().equals(roleCode)) {
            List<AdminMenuResult> menus = adminPermissionRepository.findAllMenus();
            return new AdminAccessResult(
                    List.of(AdminRole.SUPER_ADMIN.name()),
                    menus.stream()
                            .filter(menu -> !"BUTTON".equals(menu.menuType()))
                            .map(AdminMenuResult::menuCode)
                            .filter(code -> code != null && !code.isBlank())
                            .distinct()
                            .sorted()
                            .toList(),
                    menus.stream()
                            .map(AdminMenuResult::permissionCode)
                            .filter(code -> code != null && !code.isBlank())
                            .distinct()
                            .sorted()
                            .toList()
            );
        }
        return new AdminAccessResult(
                adminPermissionRepository.findRoleKeysByAdminId(adminId),
                adminPermissionRepository.findMenuCodesByAdminId(adminId),
                adminPermissionRepository.findPermissionCodesByAdminId(adminId)
        );
    }

    public void ensureSuperAdmin(AuthenticatedAccount account) {
        if (account == null
                || !AccountType.ADMIN.name().equals(account.accountType())
                || !AdminRole.SUPER_ADMIN.name().equals(account.roleCode())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private SaveAdminRoleCommand normalize(SaveAdminRoleCommand command, boolean requireRoleKey) {
        if (command == null
                || command.roleName() == null
                || command.roleName().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        String roleKey = command.roleKey();
        if (requireRoleKey) {
            if (roleKey == null || roleKey.isBlank()) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR);
            }
            roleKey = roleKey.trim().toUpperCase(Locale.ROOT);
            if (!roleKey.matches("^[A-Z0-9_]{2,32}$")) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR);
            }
        }
        return new SaveAdminRoleCommand(
                roleKey,
                command.roleName().trim(),
                "DISABLED".equals(command.status()) ? "DISABLED" : "ENABLED",
                command.sortOrder() == null ? 100 : command.sortOrder(),
                command.remark(),
                command.menuIds() == null ? List.of() : command.menuIds()
        );
    }
}
