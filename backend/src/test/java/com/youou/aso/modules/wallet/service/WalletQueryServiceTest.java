package com.youou.aso.modules.wallet.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.WalletAccount;
import com.youou.aso.modules.wallet.domain.WalletDirection;
import com.youou.aso.modules.wallet.domain.WalletTransaction;
import com.youou.aso.modules.wallet.domain.WalletTransactionType;
import com.youou.aso.modules.wallet.dto.WalletOverviewResult;
import com.youou.aso.modules.wallet.dto.WalletTransactionResult;
import com.youou.aso.modules.wallet.repository.WalletQueryRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletQueryServiceTest {
    private final FakeWalletQueryRepository repository = new FakeWalletQueryRepository();
    private final WalletQueryService service = new WalletQueryService(repository);

    @Test
    void getCustomerWalletReturnsCurrentBalance() {
        repository.wallet = walletAccount(10L, "88.50", "2.00");

        WalletOverviewResult result = service.getCustomerWallet(10L);

        assertThat(result.customerId()).isEqualTo(10L);
        assertThat(result.balance()).isEqualByComparingTo("88.50");
        assertThat(result.frozenBalance()).isEqualByComparingTo("2.00");
    }

    @Test
    void listCustomerTransactionsLimitsRowsAndKeepsOnlyCurrentCustomer() {
        repository.transactions = List.of(
                transaction(1L, 10L, "WT1", WalletDirection.DEBIT, WalletTransactionType.ORDER_DEDUCT, "12.00"),
                transaction(2L, 10L, "WT2", WalletDirection.CREDIT, WalletTransactionType.ADMIN_RECHARGE, "20.00"),
                transaction(3L, 11L, "WT3", WalletDirection.DEBIT, WalletTransactionType.ORDER_DEDUCT, "5.00")
        );

        List<WalletTransactionResult> results = service.listCustomerTransactions(10L, 1);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).customerId()).isEqualTo(10L);
        assertThat(results.get(0).transactionNo()).isEqualTo("WT1");
        assertThat(repository.lastCustomerTransactionLimit).isEqualTo(1);
    }

    @Test
    void listAdminTransactionsAllowsCustomerFilterAndCapsLargeLimit() {
        repository.transactions = List.of(
                transaction(1L, 10L, "WT1", WalletDirection.DEBIT, WalletTransactionType.ORDER_DEDUCT, "12.00"),
                transaction(2L, 11L, "WT2", WalletDirection.CREDIT, WalletTransactionType.ADMIN_RECHARGE, "20.00")
        );

        List<WalletTransactionResult> results = service.listAdminTransactions(11L, null, 500);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).customerId()).isEqualTo(11L);
        assertThat(repository.lastAdminTransactionLimit).isEqualTo(100);
    }

    @Test
    void listAdminTransactionsAllowsTypeFilter() {
        repository.transactions = List.of(
                transaction(1L, 10L, "WT1", WalletDirection.DEBIT, WalletTransactionType.ORDER_DEDUCT, "12.00"),
                transaction(2L, 10L, "WT2", WalletDirection.CREDIT, WalletTransactionType.ADMIN_RECHARGE, "20.00"),
                transaction(3L, 11L, "WT3", WalletDirection.CREDIT, WalletTransactionType.ADMIN_RECHARGE, "30.00")
        );

        List<WalletTransactionResult> results = service.listAdminTransactions(10L, WalletTransactionType.ADMIN_RECHARGE, 50);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).transactionNo()).isEqualTo("WT2");
        assertThat(repository.lastAdminTransactionType).isEqualTo(WalletTransactionType.ADMIN_RECHARGE);
    }

    @Test
    void getCustomerWalletRejectsMissingWallet() {
        repository.wallet = null;

        assertThatThrownBy(() -> service.getCustomerWallet(10L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WALLET_NOT_FOUND);
    }

    private WalletAccount walletAccount(Long customerId, String balance, String frozenBalance) {
        WalletAccount wallet = new WalletAccount();
        wallet.setId(1L);
        wallet.setCustomerId(customerId);
        wallet.setBalance(new BigDecimal(balance));
        wallet.setFrozenBalance(new BigDecimal(frozenBalance));
        wallet.setVersion(0L);
        return wallet;
    }

    private WalletTransaction transaction(
            Long id,
            Long customerId,
            String transactionNo,
            WalletDirection direction,
            WalletTransactionType type,
            String amount
    ) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setId(id);
        transaction.setCustomerId(customerId);
        transaction.setWalletId(1L);
        transaction.setTransactionNo(transactionNo);
        transaction.setDirection(direction);
        transaction.setTransactionType(type);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setBalanceBefore(new BigDecimal("100.00"));
        transaction.setBalanceAfter(new BigDecimal("88.00"));
        transaction.setRemark("test");
        transaction.setCreatedAt(LocalDateTime.of(2026, 6, 20, 12, 0));
        return transaction;
    }

    private static final class FakeWalletQueryRepository implements WalletQueryRepository {
        private WalletAccount wallet;
        private List<WalletTransaction> transactions = new ArrayList<>();
        private int lastCustomerTransactionLimit;
        private int lastAdminTransactionLimit;
        private WalletTransactionType lastAdminTransactionType;

        @Override
        public Optional<WalletAccount> findAccountByCustomerId(Long customerId) {
            if (wallet == null || !wallet.getCustomerId().equals(customerId)) {
                return Optional.empty();
            }
            return Optional.of(wallet);
        }

        @Override
        public List<WalletTransaction> findTransactionsByCustomerId(Long customerId, int limit) {
            lastCustomerTransactionLimit = limit;
            return transactions.stream()
                    .filter(transaction -> transaction.getCustomerId().equals(customerId))
                    .limit(limit)
                    .toList();
        }

        @Override
        public List<WalletTransaction> findTransactions(Long customerId, WalletTransactionType transactionType, int limit) {
            lastAdminTransactionLimit = limit;
            lastAdminTransactionType = transactionType;
            return transactions.stream()
                    .filter(transaction -> customerId == null || transaction.getCustomerId().equals(customerId))
                    .filter(transaction -> transactionType == null || transaction.getTransactionType() == transactionType)
                    .limit(limit)
                    .toList();
        }
    }
}
