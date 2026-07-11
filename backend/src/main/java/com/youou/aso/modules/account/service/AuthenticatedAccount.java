package com.youou.aso.modules.account.service;

public record AuthenticatedAccount(
        Long accountId,
        String accountType,
        String roleCode
) {
}
