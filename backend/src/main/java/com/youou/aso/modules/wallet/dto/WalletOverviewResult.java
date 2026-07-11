package com.youou.aso.modules.wallet.dto;

import com.youou.aso.modules.account.domain.WalletAccount;

import java.math.BigDecimal;

public record WalletOverviewResult(
        Long customerId,
        BigDecimal balance,
        BigDecimal frozenBalance
) {
    public static WalletOverviewResult from(WalletAccount wallet) {
        return new WalletOverviewResult(
                wallet.getCustomerId(),
                wallet.getBalance(),
                wallet.getFrozenBalance()
        );
    }
}
