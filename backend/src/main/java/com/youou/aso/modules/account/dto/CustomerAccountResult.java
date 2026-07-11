package com.youou.aso.modules.account.dto;

import com.youou.aso.modules.account.domain.AccountStatus;
import com.youou.aso.modules.account.domain.CustomerAccount;
import com.youou.aso.modules.account.domain.WalletAccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CustomerAccountResult(
        Long id,
        String username,
        String email,
        AccountStatus status,
        boolean forcePasswordChange,
        String preferredLocale,
        BigDecimal balance,
        BigDecimal frozenBalance,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CustomerAccountResult from(CustomerAccount customer, WalletAccount wallet) {
        return new CustomerAccountResult(
                customer.getId(),
                customer.getUsername(),
                customer.getEmail(),
                customer.getStatus(),
                customer.isForcePasswordChange(),
                customer.getPreferredLocale(),
                wallet == null ? BigDecimal.ZERO : wallet.getBalance(),
                wallet == null ? BigDecimal.ZERO : wallet.getFrozenBalance(),
                customer.getLastLoginAt(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
