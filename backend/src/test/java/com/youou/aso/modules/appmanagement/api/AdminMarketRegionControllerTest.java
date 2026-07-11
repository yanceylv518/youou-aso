package com.youou.aso.modules.appmanagement.api;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.appmanagement.dto.AdminMarketRegionResult;
import com.youou.aso.modules.appmanagement.service.AdminMarketRegionService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminMarketRegionControllerTest {
    private final AdminMarketRegionService adminMarketRegionService = mock(AdminMarketRegionService.class);
    private final AdminMarketRegionController controller = new AdminMarketRegionController(adminMarketRegionService);

    @Test
    void adminCanListAllRegions() {
        AdminMarketRegionResult region = sampleRegion(true);
        when(adminMarketRegionService.listRegions()).thenReturn(List.of(region));

        List<AdminMarketRegionResult> result = controller.list(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "ADMIN")
        ).data();

        assertThat(result).containsExactly(region);
        verify(adminMarketRegionService).listRegions();
    }

    @Test
    void adminCanUpdateRegionSwitches() {
        AdminMarketRegionResult region = sampleRegion(false);
        when(adminMarketRegionService.updateRegion("US", false, true, false, false)).thenReturn(region);

        AdminMarketRegionResult result = controller.update(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "ADMIN"),
                "US",
                new AdminMarketRegionController.UpdateMarketRegionRequest(false, true, false, false)
        ).data();

        assertThat(result.enabled()).isFalse();
        verify(adminMarketRegionService).updateRegion("US", false, true, false, false);
    }

    @Test
    void customerCannotUseAdminRegionEndpoint() {
        assertThatThrownBy(() -> controller.list(
                new AuthenticatedAccount(10L, AccountType.CUSTOMER.name(), "CUSTOMER")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    private AdminMarketRegionResult sampleRegion(boolean enabled) {
        return new AdminMarketRegionResult("US", "美国", "United States", enabled, true, true, true, 1);
    }
}
