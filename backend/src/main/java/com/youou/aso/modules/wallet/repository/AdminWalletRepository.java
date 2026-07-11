package com.youou.aso.modules.wallet.repository;

import com.youou.aso.modules.account.domain.WalletAccount;
import com.youou.aso.modules.wallet.domain.WalletTransaction;

import java.math.BigDecimal;
import java.util.Optional;

public interface AdminWalletRepository {
    Optional<WalletAccount> findByCustomerIdForUpdate(Long customerId);

    void updateBalance(Long walletId, BigDecimal balance);

    WalletTransaction saveTransaction(WalletTransaction transaction);
}
