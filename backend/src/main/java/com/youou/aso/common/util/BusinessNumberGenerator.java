package com.youou.aso.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class BusinessNumberGenerator {
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("yyMMdd");

    private BusinessNumberGenerator() {
    }

    public static String generate(String prefix, LocalDateTime now) {
        long suffix = Math.abs(System.nanoTime() % 1_000_000L);
        return prefix + now.format(SHORT_DATE) + String.format("%06d", suffix);
    }
}
