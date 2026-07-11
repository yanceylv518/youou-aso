package com.youou.aso.modules.appmanagement.api;

import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.appmanagement.dto.CreateManualCustomerAppCommand;
import com.youou.aso.modules.appmanagement.dto.CustomerAppResult;
import com.youou.aso.modules.appmanagement.service.CustomerAppService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerAppControllerTest {
    private final CustomerAppService customerAppService = mock(CustomerAppService.class);
    private final CustomerAppController controller = new CustomerAppController(customerAppService);

    @Test
    void customerCanCreateManualAppForSelf() {
        CustomerAppResult expected = sampleResult(10L);
        when(customerAppService.createManual(eq(10L), argThat(command ->
                command.storeType() == StoreType.GOOGLE_PLAY
                        && "US".equals(command.regionCode())
                        && "com.youou.manual".equals(command.appIdentifier())
                        && "Manual Demo".equals(command.appName())
                        && "https://example.com/icon.png".equals(command.appIconUrl())
                        && "Tools".equals(command.categoryHint())
        ))).thenReturn(expected);

        CustomerAppResult result = controller.create(
                new AuthenticatedAccount(10L, AccountType.CUSTOMER.name(), "CUSTOMER"),
                new CustomerAppController.CreateCustomerAppRequest(
                        StoreType.GOOGLE_PLAY,
                        "US",
                        "com.youou.manual",
                        "Tools",
                        "Manual Demo",
                        "https://example.com/icon.png"
                )
        ).data();

        assertThat(result).isEqualTo(expected);
        verify(customerAppService).createManual(eq(10L), argThat(command ->
                command instanceof CreateManualCustomerAppCommand
                        && "Manual Demo".equals(command.appName())
        ));
    }

    private CustomerAppResult sampleResult(Long customerId) {
        return new CustomerAppResult(
                1L,
                customerId,
                StoreType.GOOGLE_PLAY,
                "US",
                "com.youou.manual",
                "Manual Demo",
                "https://example.com/icon.png",
                "com.youou.manual",
                "com.youou.manual",
                "Tools",
                List.of("US"),
                "demo",
                "demo@example.com",
                "ACTIVE",
                LocalDateTime.of(2026, 6, 30, 8, 0),
                LocalDateTime.of(2026, 6, 30, 8, 0)
        );
    }
}
