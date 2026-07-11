package com.youou.aso.modules.account.dto;

public record ChangePasswordCommand(
        String oldPassword,
        String newPassword,
        String confirmPassword
) {
}
