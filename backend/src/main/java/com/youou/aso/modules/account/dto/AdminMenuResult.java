package com.youou.aso.modules.account.dto;

public record AdminMenuResult(
        Long id,
        Long parentId,
        String menuType,
        String menuCode,
        String nameZh,
        String nameEn,
        String routePath,
        String permissionCode,
        String icon,
        int sortOrder,
        boolean visible,
        String status
) {
}
