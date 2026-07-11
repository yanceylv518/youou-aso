package com.youou.aso.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionMailConfigTest {
    @Test
    void productionMailConfigSupportsSslForPort465Providers() throws IOException {
        String prodConfig = new String(
                getClass().getResourceAsStream("/application-prod.yml").readAllBytes(),
                StandardCharsets.UTF_8
        );

        assertThat(prodConfig).contains("ssl:");
        assertThat(prodConfig).contains("enable: ${YOUOU_MAIL_SSL_ENABLE:false}");
    }

    @Test
    void productionMailConfigKeepsBackwardCompatiblePasswordAndFromFallbacks() throws IOException {
        String prodConfig = new String(
                getClass().getResourceAsStream("/application-prod.yml").readAllBytes(),
                StandardCharsets.UTF_8
        );

        assertThat(prodConfig).contains("password: ${YOUOU_MAIL_APP_PASSWORD:${YOUOU_MAIL_PASSWORD:}}");
        assertThat(prodConfig).contains("from: ${YOUOU_MAIL_FROM:${YOUOU_MAIL_USERNAME:}}");
    }
}
