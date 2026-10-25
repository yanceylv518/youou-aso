package com.youou.aso.modules.account.dto;

public record RegisterCustomerCommand(
        String username,
        String email,
        String password,
        String inviteCode
) {
    public RegisterCustomerCommand(String username, String email, String password) {
        this(username, email, password, null);
    }
}
