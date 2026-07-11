package com.youou.aso.modules.account.dto;

public record RegisterCustomerResult(
        Long accountId,
        String username,
        String email
) {
}
