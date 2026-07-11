package com.youou.aso.modules.wallet.dto;

import com.youou.aso.modules.wallet.domain.WalletTransactionType;
import com.youou.aso.modules.wallet.domain.WalletTransactionTypeConfig;

import java.time.LocalDateTime;

public record WalletTransactionTypeConfigResult(
        WalletTransactionType transactionType,
        String displayNameZh,
        String displayNameEn,
        LocalDateTime updatedAt
) {
    public static WalletTransactionTypeConfigResult from(WalletTransactionTypeConfig config) {
        return new WalletTransactionTypeConfigResult(
                config.getTransactionType(),
                config.getDisplayNameZh(),
                config.getDisplayNameEn(),
                config.getUpdatedAt()
        );
    }
}
