package com.youou.aso.modules.account.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdminRoleResult(
        Long id,
        String roleKey,
        String roleName,
        String status,
        int sortOrder,
        boolean builtIn,
        String remark,
        List<Long> menuIds,
        List<String> permissionCodes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
