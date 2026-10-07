package com.youou.aso.modules.account.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountStatus;
import com.youou.aso.modules.account.domain.AdminAccount;
import com.youou.aso.modules.account.domain.AdminRole;
import com.youou.aso.modules.account.dto.AdminAccountResult;
import com.youou.aso.modules.account.dto.CreateAdminAccountCommand;
import com.youou.aso.modules.account.repository.AdminAccountRepository;
import com.youou.aso.modules.account.repository.CustomerAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminAccountService {
    private final AdminAccountRepository adminAccountRepository;
    private final CustomerAccountRepository customerAccountRepository;
    private final AdminPermissionService adminPermissionService;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountService(
            AdminAccountRepository adminAccountRepository,
            CustomerAccountRepository customerAccountRepository,
            AdminPermissionService adminPermissionService,
            PasswordEncoder passwordEncoder
    ) {
        this.adminAccountRepository = adminAccountRepository;
        this.customerAccountRepository = customerAccountRepository;
        this.adminPermissionService = adminPermissionService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AdminAccountResult> listAdmins() {
        return adminAccountRepository.findAll().stream()
                .map(this::toResult)
                .toList();
    }

    @Transactional
    public AdminAccountResult createAdmin(CreateAdminAccountCommand command) {
        customerAccountRepository.lockRegistration();
        String username = command.username().trim();
        String email = command.email().trim().toLowerCase();
        if (adminAccountRepository.existsByUsername(username) || customerAccountRepository.existsByUsername(username) || adminAccountRepository.existsByEmail(username) || customerAccountRepository.existsByEmail(username)) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (adminAccountRepository.existsByEmail(email) || customerAccountRepository.existsByEmail(email) || adminAccountRepository.existsByUsername(email) || customerAccountRepository.existsByUsername(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        AdminAccount admin = new AdminAccount();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(command.password()));
        admin.setRoleCode(AdminRole.ADMIN);
        admin.setStatus(AccountStatus.ENABLED);
        admin.setForcePasswordChange(false);
        admin.setPreferredLocale("zh-CN");
        AdminAccount saved = adminAccountRepository.save(admin);
        adminPermissionService.replaceAdminRoles(saved.getId(), command.roleIds());
        return toResult(saved);
    }

    public AdminAccountResult updateRoles(Long adminId, List<Long> roleIds) {
        AdminAccount admin = adminAccountRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (admin.getRoleCode() == AdminRole.SUPER_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        adminPermissionService.replaceAdminRoles(adminId, roleIds);
        AdminAccount updated = adminAccountRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        return toResult(updated);
    }

    public List<Long> getPermissions(Long adminId) {
        AdminAccount admin = adminAccountRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (AdminRole.SUPER_ADMIN.equals(admin.getRoleCode())) {
            return adminPermissionService.listMenus().stream().map(menu -> menu.id()).toList();
        }
        return adminPermissionService.findMenuIdsByAdminId(adminId);
    }

    @Transactional
    public List<Long> updatePermissions(Long adminId, List<Long> menuIds) {
        AdminAccount admin = adminAccountRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (AdminRole.SUPER_ADMIN.equals(admin.getRoleCode())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        adminPermissionService.replaceAdminMenus(adminId, menuIds);
        return adminPermissionService.findMenuIdsByAdminId(adminId);
    }

    public AdminAccountResult updateStatus(Long operatorAdminId, Long adminId, AccountStatus status) {
        if (status == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (operatorAdminId.equals(adminId) && status != AccountStatus.ENABLED) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        adminAccountRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        adminAccountRepository.updateStatus(adminId, status);
        AdminAccount updated = adminAccountRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        return toResult(updated);
    }

    private AdminAccountResult toResult(AdminAccount admin) {
        if (admin.getRoleCode() == AdminRole.SUPER_ADMIN) {
            return AdminAccountResult.from(admin, List.of(), List.of(AdminRole.SUPER_ADMIN.name()));
        }
        return AdminAccountResult.from(
                admin,
                adminPermissionService.findRoleIdsByAdminId(admin.getId()),
                adminPermissionService.accessForAdmin(admin.getId(), admin.getRoleCode().name()).roleKeys()
        );
    }
}
