package com.youou.aso.modules.account.dto;

import java.util.List;

public record CurrentAccountResult(
        Long accountId,
        String username,
        String email,
        String accountType,
        String roleCode,
        boolean forcePasswordChange,
        String preferredLocale,
        List<String> roleKeys,
        List<String> menuCodes,
        List<String> permissions
) {
}