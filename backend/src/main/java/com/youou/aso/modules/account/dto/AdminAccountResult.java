package com.youou.aso.modules.account.dto;

import com.youou.aso.modules.account.domain.AccountStatus;
import com.youou.aso.modules.account.domain.AdminAccount;
import com.youou.aso.modules.account.domain.AdminRole;

import java.time.LocalDateTime;
import java.util.List;

public record AdminAccountResult(
        Long id,
        String username,
        String email,
        AdminRole roleCode,
        List<Long> roleIds,
        List<String> roleKeys,
        AccountStatus status,
        boolean forcePasswordChange,
        String preferredLocale,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public AdminAccountResult(
            Long id,
            String username,
            String email,
            AdminRole roleCode,
            AccountStatus status,
            boolean forcePasswordChange,
            String preferredLocale,
            LocalDateTime lastLoginAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this(id, username, email, roleCode, List.of(), List.of(), status, forcePasswordChange, preferredLocale, lastLoginAt, createdAt, updatedAt);
    }

    public static AdminAccountResult from(AdminAccount admin) {
        return from(admin, List.of(), List.of());
    }

    public static AdminAccountResult from(AdminAccount admin, List<Long> roleIds, List<String> roleKeys) {
        return new AdminAccountResult(
                admin.getId(),
                admin.getUsername(),
                admin.getEmail(),
                admin.getRoleCode(),
                roleIds,
                roleKeys,
                admin.getStatus(),
                admin.isForcePasswordChange(),
                admin.getPreferredLocale(),
                admin.getLastLoginAt(),
                admin.getCreatedAt(),
                admin.getUpdatedAt()
        );
    }
}