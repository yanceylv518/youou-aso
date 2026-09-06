package com.youou.aso.modules.wallet.service;

import java.math.BigDecimal;

public interface WalletService {
    WalletDebitResult debitForOrder(Long customerId, BigDecimal amount, String remark);

    WalletDebitResult refundForOrder(Long customerId, BigDecimal amount, String remark);

    BigDecimal currentBalanceForUpdate(Long customerId);

    default void linkTransactionToOrder(Long transactionId, Long orderId) {
    }
}
