package com.youou.aso.modules.account.api;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountStatus;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.domain.AdminRole;
import com.youou.aso.modules.account.dto.AdminAccountResult;
import com.youou.aso.modules.account.service.AdminAccountService;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminAccountControllerTest {
    private final AdminAccountService adminAccountService = mock(AdminAccountService.class);
    private final AdminAccountController controller = new AdminAccountController(adminAccountService);

    @Test
    void superAdminCanListAdminAccounts() {
        AdminAccountResult admin = sampleAdmin(AccountStatus.ENABLED);
        when(adminAccountService.listAdmins()).thenReturn(List.of(admin));

        List<AdminAccountResult> result = controller.list(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), AdminRole.SUPER_ADMIN.name())
        ).data();

        assertThat(result).containsExactly(admin);
        verify(adminAccountService).listAdmins();
    }

    @Test
    void superAdminCanUpdateAdminStatus() {
        AdminAccountResult disabled = sampleAdmin(AccountStatus.DISABLED);
        when(adminAccountService.updateStatus(2L, 3L, AccountStatus.DISABLED)).thenReturn(disabled);

        AdminAccountResult result = controller.updateStatus(
                new AuthenticatedAccount(2L, AccountType.ADMIN.name(), AdminRole.SUPER_ADMIN.name()),
                3L,
                new AdminAccountController.UpdateAdminStatusRequest(AccountStatus.DISABLED)
        ).data();

        assertThat(result.status()).isEqualTo(AccountStatus.DISABLED);
        verify(adminAccountService).updateStatus(2L, 3L, AccountStatus.DISABLED);
    }

    @Test
    void superAdminCanCreateAdminAccount() {
        AdminAccountResult created = sampleAdmin(AccountStatus.ENABLED);
        when(adminAccountService.createAdmin(org.mockito.ArgumentMatchers.any())).thenReturn(created);

        AdminAccountResult result = controller.create(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), AdminRole.SUPER_ADMIN.name()),
                new AdminAccountController.CreateAdminRequest("ops_admin", "ops@example.com", "StrongPass123")
        ).data();

        assertThat(result.username()).isEqualTo("admin");
        verify(adminAccountService).createAdmin(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void regularAdminCannotUseAdminAccountEndpoint() {
        assertThatThrownBy(() -> controller.list(
                new AuthenticatedAccount(2L, AccountType.ADMIN.name(), AdminRole.ADMIN.name())
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    private AdminAccountResult sampleAdmin(AccountStatus status) {
        return new AdminAccountResult(
                3L,
                "admin",
                "admin@example.com",
                AdminRole.ADMIN,
                status,
                false,
                "zh-CN",
                null,
                LocalDateTime.of(2026, 6, 20, 10, 0),
                LocalDateTime.of(2026, 6, 20, 10, 0)
        );
    }
}
