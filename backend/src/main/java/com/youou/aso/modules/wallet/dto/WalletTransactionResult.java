package com.youou.aso.modules.wallet.dto;

import com.youou.aso.modules.wallet.domain.WalletDirection;
import com.youou.aso.modules.wallet.domain.WalletTransaction;
import com.youou.aso.modules.wallet.domain.WalletTransactionType;
import com.youou.aso.modules.order.domain.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletTransactionResult(
        Long id,
        String transactionNo,
        Long customerId,
        WalletDirection direction,
        WalletTransactionType transactionType,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        Long relatedOrderId,
        String orderNo,
        OrderType orderType,
        String appName,
        String remark,
        LocalDateTime createdAt
) {
    public static WalletTransactionResult from(WalletTransaction transaction) {
        return new WalletTransactionResult(
                transaction.getId(),
                transaction.getTransactionNo(),
                transaction.getCustomerId(),
                transaction.getDirection(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getBalanceBefore(),
                transaction.getBalanceAfter(),
                transaction.getRelatedOrderId(),
                transaction.getOrderNo(),
                transaction.getOrderType(),
                transaction.getAppName(),
                transaction.getRemark(),
                transaction.getCreatedAt()
        );
    }
}
