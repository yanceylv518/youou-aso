package com.youou.aso.modules.support.dto;

public record UpdateHomeMetricsCommand(
        String appsValue,
        String satisfactionValue,
        int experienceYears,
        String teamValue
) {
}
