package com.youou.aso.modules.account.dto;

import java.util.List;

public record SaveAdminRoleCommand(
        String roleKey,
        String roleName,
        String status,
        Integer sortOrder,
        String remark,
        List<Long> menuIds
) {
}
