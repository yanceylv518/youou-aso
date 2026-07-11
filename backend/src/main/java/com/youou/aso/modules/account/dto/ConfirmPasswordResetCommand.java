package com.youou.aso.modules.account.dto;

public record ConfirmPasswordResetCommand(
        String email,
        String code,
        String newPassword,
        String confirmPassword
) {
}
