package com.youou.aso.modules.wallet.service;

import java.math.BigDecimal;

public record WalletDebitResult(
        Long transactionId,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter
) {
}
