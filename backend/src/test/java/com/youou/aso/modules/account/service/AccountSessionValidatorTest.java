package com.youou.aso.modules.account.service;

import com.youou.aso.modules.account.domain.*;
import com.youou.aso.modules.account.repository.*;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class AccountSessionValidatorTest {
    private final CustomerAccountRepository customers = mock(CustomerAccountRepository.class);
    private final AdminAccountRepository admins = mock(AdminAccountRepository.class);
    private final JwtTokenService tokens = new JwtTokenService("tests", "test-signing-secret-at-least-thirty-two-bytes", 3600);
    private final AccountSessionValidator validator = new AccountSessionValidator(customers, admins, tokens);

    @Test void filterAuthenticatesCurrentSessionAndClearsRevokedSession() throws Exception {
        var account = new CustomerAccount();
        account.setId(1L); account.setStatus(AccountStatus.ENABLED); account.setPasswordHash("hash");
        when(customers.findById(1L)).thenReturn(Optional.of(account));
        String token = tokens.issue(1L, "CUSTOMER", "", "hash");
        var filter = new JwtAuthenticationFilter(validator);
        try {
            for (var status : java.util.List.of(AccountStatus.ENABLED, AccountStatus.DISABLED)) {
                account.setStatus(status);
                var request = new org.springframework.mock.web.MockHttpServletRequest();
                request.addHeader("Authorization", "Bearer " + token);
                filter.doFilter(request, new org.springframework.mock.web.MockHttpServletResponse(), (req, res) -> {
                    var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                    if (status == AccountStatus.ENABLED) assertThat(authentication).isNotNull();
                    else assertThat(authentication).isNull();
                });
            }
        } finally { org.springframework.security.core.context.SecurityContextHolder.clearContext(); }
    }

    @Test void customerSessionRevokedOnPasswordChangeDisableLockOrDeletion() {
        var account = new CustomerAccount();
        account.setId(1L); account.setStatus(AccountStatus.ENABLED); account.setPasswordHash("first-hash");
        when(customers.findById(1L)).thenReturn(Optional.of(account));
        String token = tokens.issue(1L, "CUSTOMER", "", account.getPasswordHash());
        assertThat(validator.validate(token).accountId()).isEqualTo(1L);
        account.setPasswordHash("changed-hash");
        assertThatThrownBy(() -> validator.validate(token)).isInstanceOf(SecurityException.class);
        account.setPasswordHash("first-hash");
        for (var status : java.util.List.of(AccountStatus.DISABLED, AccountStatus.LOCKED)) {
            account.setStatus(status);
            assertThatThrownBy(() -> validator.validate(token)).isInstanceOf(SecurityException.class);
        }
        when(customers.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> validator.validate(token)).isInstanceOf(SecurityException.class);
    }

    @Test void adminRoleComesFromDatabaseAndLegacyTokensRequireLogin() {
        var account = new AdminAccount();
        account.setId(7L); account.setStatus(AccountStatus.ENABLED); account.setPasswordHash("hash"); account.setRoleCode(AdminRole.ADMIN);
        when(admins.findById(7L)).thenReturn(Optional.of(account));
        String token = tokens.issue(7L, "ADMIN", "SUPER_ADMIN", "hash");
        assertThat(validator.validate(token).roleCode()).isEqualTo("ADMIN");
        assertThatThrownBy(() -> validator.validate(tokens.issue(7L, "ADMIN", "SUPER_ADMIN"))).isInstanceOf(SecurityException.class);
        account.setStatus(AccountStatus.DISABLED);
        assertThatThrownBy(() -> validator.validate(token)).isInstanceOf(SecurityException.class);
        account.setStatus(AccountStatus.ENABLED); account.setPasswordHash("new-hash");
        assertThatThrownBy(() -> validator.validate(token)).isInstanceOf(SecurityException.class);
    }
}
