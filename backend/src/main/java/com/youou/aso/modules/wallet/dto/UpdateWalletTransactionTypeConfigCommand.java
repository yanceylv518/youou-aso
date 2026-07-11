package com.youou.aso.modules.wallet.dto;

import com.youou.aso.modules.wallet.domain.WalletTransactionType;

public record UpdateWalletTransactionTypeConfigCommand(
        WalletTransactionType transactionType,
        String displayNameZh,
        String displayNameEn
) {
}
