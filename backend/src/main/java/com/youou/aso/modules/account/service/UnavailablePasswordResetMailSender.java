package com.youou.aso.modules.account.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;

public class UnavailablePasswordResetMailSender implements PasswordResetMailSender {
    @Override
    public void sendResetCode(String email, String code) {
        throw new BusinessException(ErrorCode.PASSWORD_RESET_MAIL_UNAVAILABLE);
    }
}
