package com.youou.aso.modules.account.service;

import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.domain.AdminRole;
import com.youou.aso.modules.account.repository.AdminPermissionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component("perm")
public class AdminPermissionEvaluator {
    private final AdminPermissionRepository adminPermissionRepository;

    public AdminPermissionEvaluator(AdminPermissionRepository adminPermissionRepository) {
        this.adminPermissionRepository = adminPermissionRepository;
    }

    public boolean has(String permissionCode) {
        AuthenticatedAccount account = currentAccount();
        if (!isAdmin(account) || permissionCode == null || permissionCode.isBlank()) {
            return false;
        }
        if (isSuperAdmin(account)) {
            return true;
        }
        return adminPermissionRepository.findPermissionCodesByAdminId(account.accountId()).contains(permissionCode);
    }

    public boolean hasAny(String... permissionCodes) {
        if (permissionCodes == null || permissionCodes.length == 0) {
            return false;
        }
        return Arrays.stream(permissionCodes).anyMatch(this::has);
    }

    public boolean hasMenu(String menuCode) {
        AuthenticatedAccount account = currentAccount();
        if (!isAdmin(account) || menuCode == null || menuCode.isBlank()) {
            return false;
        }
        if (isSuperAdmin(account)) {
            return true;
        }
        return adminPermissionRepository.findMenuCodesByAdminId(account.accountId()).contains(menuCode);
    }

    public boolean superAdmin() {
        return isSuperAdmin(currentAccount());
    }

    private AuthenticatedAccount currentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedAccount account)) {
            return null;
        }
        return account;
    }

    private boolean isAdmin(AuthenticatedAccount account) {
        return account != null && AccountType.ADMIN.name().equals(account.accountType());
    }

    private boolean isSuperAdmin(AuthenticatedAccount account) {
        return isAdmin(account) && AdminRole.SUPER_ADMIN.name().equals(account.roleCode());
    }
}