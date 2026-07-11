package com.youou.aso.common.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessNumberGeneratorTest {
    @Test
    void generateUsesPrefixShortDateAndSixDigitSuffix() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 20, 8, 30, 15);

        assertThat(BusinessNumberGenerator.generate("YO", now))
                .hasSize(14)
                .matches("YO260620\\d{6}");
        assertThat(BusinessNumberGenerator.generate("WT", now))
                .hasSize(14)
                .matches("WT260620\\d{6}");
    }
}
