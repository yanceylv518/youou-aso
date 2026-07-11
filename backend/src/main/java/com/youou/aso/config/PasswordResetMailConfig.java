package com.youou.aso.config;

import com.youou.aso.modules.account.service.PasswordResetMailSender;
import com.youou.aso.modules.account.service.UnavailablePasswordResetMailSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PasswordResetMailConfig {
    @Bean
    @ConditionalOnMissingBean(PasswordResetMailSender.class)
    PasswordResetMailSender unavailablePasswordResetMailSender() {
        return new UnavailablePasswordResetMailSender();
    }
}
