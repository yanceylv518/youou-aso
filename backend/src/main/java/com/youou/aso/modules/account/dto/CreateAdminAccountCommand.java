package com.youou.aso.modules.account.dto;

import java.util.List;

public record CreateAdminAccountCommand(
        String username,
        String email,
        String password,
        List<Long> roleIds
) {
    public CreateAdminAccountCommand(String username, String email, String password) {
        this(username, email, password, List.of());
    }
}