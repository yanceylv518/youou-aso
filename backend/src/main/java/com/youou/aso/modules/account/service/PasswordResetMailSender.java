package com.youou.aso.modules.account.service;

public interface PasswordResetMailSender {
    void sendResetCode(String email, String code);
}
