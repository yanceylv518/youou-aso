package com.youou.aso.modules.account.repository;

import com.youou.aso.modules.account.domain.WalletAccount;

public interface WalletAccountRepository {
    WalletAccount save(WalletAccount walletAccount);
}
