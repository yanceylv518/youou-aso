package com.youou.aso.config;

import com.youou.aso.modules.order.service.NoopOrderNotificationSender;
import com.youou.aso.modules.order.service.OrderNotificationSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderNotificationConfig {
    @Bean
    @ConditionalOnMissingBean(OrderNotificationSender.class)
    OrderNotificationSender noopOrderNotificationSender() {
        return new NoopOrderNotificationSender();
    }
}
