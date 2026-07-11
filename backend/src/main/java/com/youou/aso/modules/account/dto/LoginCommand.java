package com.youou.aso.modules.account.dto;

public record LoginCommand(
        String account,
        String password
) {
}
