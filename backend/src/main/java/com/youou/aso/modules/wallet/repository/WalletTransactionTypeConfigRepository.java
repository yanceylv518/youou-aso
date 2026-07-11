package com.youou.aso.modules.wallet.repository;

import com.youou.aso.modules.wallet.domain.WalletTransactionTypeConfig;

import java.util.List;

public interface WalletTransactionTypeConfigRepository {
    List<WalletTransactionTypeConfig> findAll();

    void saveAll(List<WalletTransactionTypeConfig> configs);
}
