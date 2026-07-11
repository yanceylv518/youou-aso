package com.youou.aso.modules.account.dto;

public record RegisterCustomerCommand(
        String username,
        String email,
        String password
) {
}
