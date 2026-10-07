package com.youou.aso.modules.account.service;

import com.youou.aso.modules.account.domain.AccountStatus;
import com.youou.aso.modules.account.repository.AdminAccountRepository;
import com.youou.aso.modules.account.repository.CustomerAccountRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountSessionValidator {
    private final CustomerAccountRepository customers;
    private final AdminAccountRepository admins;
    private final JwtTokenService tokens;

    public AccountSessionValidator(CustomerAccountRepository customers, AdminAccountRepository admins, JwtTokenService tokens) {
        this.customers = customers;
        this.admins = admins;
        this.tokens = tokens;
    }

    public AuthenticatedAccount validate(String token) {
        AuthenticatedAccount principal = tokens.parse(token);
        if ("CUSTOMER".equals(principal.accountType())) {
            var account = customers.findById(principal.accountId()).orElseThrow(SecurityException::new);
            if (account.getStatus() != AccountStatus.ENABLED || !tokens.matchesCredential(token, account.getPasswordHash())) {
                throw new SecurityException("Session revoked");
            }
            return new AuthenticatedAccount(account.getId(), "CUSTOMER", "");
        }
        if ("ADMIN".equals(principal.accountType())) {
            var account = admins.findById(principal.accountId()).orElseThrow(SecurityException::new);
            if (account.getStatus() != AccountStatus.ENABLED || !tokens.matchesCredential(token, account.getPasswordHash())) {
                throw new SecurityException("Session revoked");
            }
            return new AuthenticatedAccount(account.getId(), "ADMIN", account.getRoleCode().name());
        }
        throw new SecurityException("Unknown account type");
    }
}
