package com.youou.aso.modules.order.service;

import com.youou.aso.modules.order.domain.AsoOrder;
import com.youou.aso.modules.support.service.ConfigurableMailSender;
import com.youou.aso.modules.support.service.MailConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SmtpOrderNotificationSender implements OrderNotificationSender {
    private static final Logger log = LoggerFactory.getLogger(SmtpOrderNotificationSender.class);

    private final ConfigurableMailSender mailSender;
    private final MailConfigService mailConfigService;

    public SmtpOrderNotificationSender(
            ConfigurableMailSender mailSender,
            MailConfigService mailConfigService
    ) {
        this.mailSender = mailSender;
        this.mailConfigService = mailConfigService;
    }

    @Override
    public void notifyOrderCreated(AsoOrder order) {
        if (order == null) {
            return;
        }
        String[] to = resolveRecipients(order);
        if (to.length == 0) {
            log.debug("Order notification mail skipped because recipients are not configured.");
            return;
        }
        String from = mailSender.resolveFromAddress();
        if (from == null || from.isBlank()) {
            log.warn("Order notification mail skipped because mail from address is blank.");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("用户下单成功提醒");
        message.setText(buildBody(order));
        try {
            mailSender.send(message);
        } catch (MailException | IllegalStateException exception) {
            log.warn("Order notification mail send failed. orderNo={}, errorType={}, rootCause={}",
                    order.getOrderNo(),
                    exception.getClass().getSimpleName(),
                    rootCauseMessage(exception),
                    exception);
        }
    }

    private String[] resolveRecipients(AsoOrder order) {
        try {
            return mailConfigService.getEnabledOrderNotificationRecipients();
        } catch (RuntimeException exception) {
            log.warn("Order notification mail skipped because config is invalid. orderNo={}, errorType={}",
                    order.getOrderNo(),
                    exception.getClass().getSimpleName(),
                    exception);
            return new String[0];
        }
    }

    private String buildBody(AsoOrder order) {
        String customer = firstNonBlank(order.getCustomerUsername(), order.getCustomerEmail(), String.valueOf(order.getCustomerId()));
        return """
                用户%s下单已成功，请及时处理。

                订单号：%s
                订单类型：%s
                应用：%s
                商店：%s
                地区：%s
                金额：%s
                状态：%s
                """.formatted(
                customer,
                nullToDash(order.getOrderNo()),
                order.getOrderType() == null ? "-" : order.getOrderType().name(),
                nullToDash(order.getAppName()),
                order.getStoreType() == null ? "-" : order.getStoreType().name(),
                nullToDash(order.getRegionCode()),
                formatAmount(order.getTotalAmount()),
                order.getStatus() == null ? "-" : order.getStatus().name()
        );
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "-";
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String formatAmount(BigDecimal value) {
        return value == null ? "-" : "$" + value.stripTrailingZeros().toPlainString();
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
