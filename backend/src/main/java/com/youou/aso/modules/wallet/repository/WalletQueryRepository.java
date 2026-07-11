package com.youou.aso.modules.wallet.repository;

import com.youou.aso.modules.account.domain.WalletAccount;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.wallet.domain.WalletDirection;
import com.youou.aso.modules.wallet.domain.WalletTransaction;
import com.youou.aso.modules.wallet.domain.WalletTransactionType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WalletQueryRepository {
    Optional<WalletAccount> findAccountByCustomerId(Long customerId);

    List<WalletTransaction> findTransactionsByCustomerId(Long customerId, int limit);

    List<WalletTransaction> findTransactions(Long customerId, WalletTransactionType transactionType, int limit);

    default List<WalletTransaction> findTransactions(
            Long customerId,
            WalletTransactionType transactionType,
            WalletDirection direction,
            OrderType orderType,
            LocalDate createdDateFrom,
            LocalDate createdDateTo,
            int limit,
            int offset
    ) {
        return findTransactions(customerId, transactionType, limit + offset).stream()
                .filter(transaction -> direction == null || transaction.getDirection() == direction)
                .skip(offset)
                .limit(limit)
                .toList();
    }

    default long countTransactions(
            Long customerId,
            WalletTransactionType transactionType,
            WalletDirection direction,
            OrderType orderType,
            LocalDate createdDateFrom,
            LocalDate createdDateTo
    ) {
        return findTransactions(customerId, transactionType, Integer.MAX_VALUE).stream()
                .filter(transaction -> direction == null || transaction.getDirection() == direction)
                .count();
    }
}
