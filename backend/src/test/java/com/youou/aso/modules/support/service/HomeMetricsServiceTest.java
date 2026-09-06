package com.youou.aso.modules.support.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.modules.support.dto.HomeMetricsResult;
import com.youou.aso.modules.support.dto.UpdateHomeMetricsCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HomeMetricsServiceTest {
    private SystemConfigService systemConfigService;
    private HomeMetricsService service;

    @BeforeEach
    void setUp() {
        systemConfigService = mock(SystemConfigService.class);
        when(systemConfigService.getConfigs(org.mockito.ArgumentMatchers.anyCollection())).thenReturn(Map.of());
        service = new HomeMetricsService(systemConfigService);
    }

    @Test
    void returnsCurrentHomeValuesAsDefaults() {
        HomeMetricsResult result = service.getConfig();

        assertThat(result.appsValue()).isEqualTo("10,000+");
        assertThat(result.satisfactionValue()).isEqualTo("98.6%");
        assertThat(result.experienceYears()).isEqualTo(5);
        assertThat(result.teamValue()).isEqualTo("50+");
    }

    @Test
    void savesAllFourMetrics() {
        service.updateConfig(new UpdateHomeMetricsCommand("20,000+", "99%", 8, "80+"));

        verify(systemConfigService).save("home.metrics.apps.value", "20,000+", false, "Public home served apps metric");
        verify(systemConfigService).save("home.metrics.satisfaction.value", "99%", false, "Public home satisfaction metric");
        verify(systemConfigService).save("home.metrics.experience.years", "8", false, "Public home experience years");
        verify(systemConfigService).save("home.metrics.team.value", "80+", false, "Public home team metric");
    }

    @Test
    void rejectsInvalidExperienceYears() {
        assertThatThrownBy(() -> service.updateConfig(new UpdateHomeMetricsCommand("10", "99%", 1000, "10")))
                .isInstanceOf(BusinessException.class);
    }
}
