package com.youou.aso.modules.appmanagement.api;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.appmanagement.dto.CustomerAppResult;
import com.youou.aso.modules.appmanagement.dto.SearchStoreAppCommand;
import com.youou.aso.modules.appmanagement.service.CustomerAppService;
import com.youou.aso.modules.appmanagement.service.StoreAppSearchResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminCustomerAppControllerTest {
    private final CustomerAppService customerAppService = mock(CustomerAppService.class);
    private final AdminCustomerAppController controller = new AdminCustomerAppController(customerAppService);

    @Test
    void adminCanCreateManualAppForCustomer() {
        CustomerAppResult expected = sampleResult(10L);
        when(customerAppService.createManual(eq(10L), argThat(command ->
                command.storeType() == StoreType.APP_STORE
                        && "US".equals(command.regionCode())
                        && "123456789".equals(command.appIdentifier())
                        && "Manual Apple".equals(command.appName())
        ))).thenReturn(expected);

        CustomerAppResult result = controller.create(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "ADMIN"),
                new AdminCustomerAppController.AdminCreateCustomerAppRequest(
                        10L,
                        StoreType.APP_STORE,
                        "US",
                        "123456789",
                        "Business",
                        "Manual Apple",
                        null
                )
        ).data();

        assertThat(result).isEqualTo(expected);
        verify(customerAppService).createManual(eq(10L), argThat(command ->
                "Manual Apple".equals(command.appName())
        ));
    }

    @Test
    void customerCannotCreateAdminApp() {
        assertThatThrownBy(() -> controller.create(
                new AuthenticatedAccount(10L, AccountType.CUSTOMER.name(), "CUSTOMER"),
                new AdminCustomerAppController.AdminCreateCustomerAppRequest(
                        10L,
                        StoreType.APP_STORE,
                        "US",
                        "123456789",
                        null,
                        "Manual Apple",
                        null
                )
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void adminCanSearchStoreApps() {
        StoreAppSearchResult expected = new StoreAppSearchResult(
                StoreType.GOOGLE_PLAY,
                "US",
                "com.youou.demo",
                "Demo App",
                "/uploads/app-icons/demo.png",
                "com.youou.demo",
                "com.youou.demo",
                "Tools",
                "Youou"
        );
        when(customerAppService.search(argThat(command ->
                command instanceof SearchStoreAppCommand
                        && command.storeType() == StoreType.GOOGLE_PLAY
                        && "US".equals(command.regionCode())
                        && "demo".equals(command.keyword())
                        && command.limit() == 10
        ))).thenReturn(List.of(expected));

        List<StoreAppSearchResult> result = controller.search(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "ADMIN"),
                StoreType.GOOGLE_PLAY,
                "US",
                "demo",
                10
        ).data();

        assertThat(result).containsExactly(expected);
    }

    @Test
    void customerCannotSearchAdminStoreApps() {
        assertThatThrownBy(() -> controller.search(
                new AuthenticatedAccount(10L, AccountType.CUSTOMER.name(), "CUSTOMER"),
                StoreType.GOOGLE_PLAY,
                "US",
                "demo",
                10
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    private CustomerAppResult sampleResult(Long customerId) {
        return new CustomerAppResult(
                1L,
                customerId,
                StoreType.APP_STORE,
                "US",
                "123456789",
                "Manual Apple",
                null,
                null,
                "123456789",
                "Business",
                List.of("US"),
                "demo",
                "demo@example.com",
                "ACTIVE",
                LocalDateTime.of(2026, 6, 30, 8, 0),
                LocalDateTime.of(2026, 6, 30, 8, 0)
        );
    }
}
