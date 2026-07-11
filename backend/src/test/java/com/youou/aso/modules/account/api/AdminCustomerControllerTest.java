package com.youou.aso.modules.account.api;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountStatus;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.dto.CustomerAccountResult;
import com.youou.aso.modules.account.service.AdminCustomerService;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminCustomerControllerTest {
    private final AdminCustomerService adminCustomerService = mock(AdminCustomerService.class);
    private final AdminCustomerController controller = new AdminCustomerController(adminCustomerService);

    @Test
    void adminCanListCustomers() {
        CustomerAccountResult customer = sampleCustomer(AccountStatus.ENABLED);
        when(adminCustomerService.listCustomers("demo")).thenReturn(List.of(customer));

        List<CustomerAccountResult> result = controller.list(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "ADMIN"),
                "demo"
        ).data();

        assertThat(result).containsExactly(customer);
        verify(adminCustomerService).listCustomers("demo");
    }

    @Test
    void adminCanUpdateCustomerStatus() {
        CustomerAccountResult disabled = sampleCustomer(AccountStatus.DISABLED);
        when(adminCustomerService.updateStatus(10L, AccountStatus.DISABLED)).thenReturn(disabled);

        CustomerAccountResult result = controller.updateStatus(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "ADMIN"),
                10L,
                new AdminCustomerController.UpdateCustomerStatusRequest(AccountStatus.DISABLED)
        ).data();

        assertThat(result.status()).isEqualTo(AccountStatus.DISABLED);
        verify(adminCustomerService).updateStatus(10L, AccountStatus.DISABLED);
    }

    @Test
    void customerCannotUseAdminCustomerEndpoint() {
        assertThatThrownBy(() -> controller.list(
                new AuthenticatedAccount(10L, AccountType.CUSTOMER.name(), "CUSTOMER"),
                null
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    private CustomerAccountResult sampleCustomer(AccountStatus status) {
        return new CustomerAccountResult(
                10L,
                "demo",
                "demo@example.com",
                status,
                false,
                "zh-CN",
                BigDecimal.TEN,
                BigDecimal.ZERO,
                null,
                LocalDateTime.of(2026, 6, 20, 10, 0),
                LocalDateTime.of(2026, 6, 20, 10, 0)
        );
    }
}
