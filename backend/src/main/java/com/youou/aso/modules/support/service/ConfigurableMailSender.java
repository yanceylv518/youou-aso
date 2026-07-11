package com.youou.aso.modules.support.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class ConfigurableMailSender {
    private final ObjectProvider<JavaMailSender> fallbackMailSenderProvider;
    private final MailConfigService mailConfigService;

    public ConfigurableMailSender(
            ObjectProvider<JavaMailSender> fallbackMailSenderProvider,
            MailConfigService mailConfigService
    ) {
        this.fallbackMailSenderProvider = fallbackMailSenderProvider;
        this.mailConfigService = mailConfigService;
    }

    public String resolveFromAddress() {
        return mailConfigService.getMailSettings().fromAddress();
    }

    public void send(SimpleMailMessage message) throws MailException {
        MailConfigService.MailSettings settings = mailConfigService.getMailSettings();
        if (message.getFrom() == null || message.getFrom().isBlank()) {
            message.setFrom(settings.fromAddress());
        }
        JavaMailSender mailSender = settings.hasSmtpServer()
                ? createMailSender(settings)
                : fallbackMailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new IllegalStateException("Mail sender is not configured");
        }
        mailSender.send(message);
    }

    private JavaMailSender createMailSender(MailConfigService.MailSettings settings) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(settings.smtpHost());
        mailSender.setPort(settings.smtpPort());
        mailSender.setUsername(settings.username());
        mailSender.setPassword(settings.password());
        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.smtp.auth", String.valueOf(settings.smtpAuth()));
        properties.put("mail.smtp.starttls.enable", String.valueOf(settings.startTlsEnabled()));
        properties.put("mail.smtp.ssl.enable", String.valueOf(settings.sslEnabled()));
        properties.put("mail.smtp.connectiontimeout", "5000");
        properties.put("mail.smtp.timeout", "5000");
        properties.put("mail.smtp.writetimeout", "5000");
        return mailSender;
    }
}
