package com.youou.aso.modules.account.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.support.service.ConfigurableMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component
public class SmtpPasswordResetMailSender implements PasswordResetMailSender {
    private static final Logger log = LoggerFactory.getLogger(SmtpPasswordResetMailSender.class);

    private final ConfigurableMailSender mailSender;

    public SmtpPasswordResetMailSender(ConfigurableMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendResetCode(String email, String code) {
        String from = mailSender.resolveFromAddress();
        if (from == null || from.isBlank()) {
            log.warn("Password reset mail is unavailable because mail from address is blank.");
            throw new BusinessException(ErrorCode.PASSWORD_RESET_MAIL_UNAVAILABLE);
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Youou-ASO password reset code");
        message.setText("""
                Your Youou-ASO password reset code is: %s

                The code is valid for 10 minutes. If you did not request this, please ignore this email.
                """.formatted(code));
        try {
            mailSender.send(message);
        } catch (MailException | IllegalStateException exception) {
            log.warn("Password reset mail send failed. errorType={}, rootCause={}",
                    exception.getClass().getSimpleName(),
                    rootCauseMessage(exception),
                    exception);
            throw new BusinessException(ErrorCode.PASSWORD_RESET_MAIL_UNAVAILABLE);
        }
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        if (message == null || message.isBlank()) {
            return current.getClass().getSimpleName();
        }
        return message.replaceAll("[\\r\\n]+", " ");
    }
}
