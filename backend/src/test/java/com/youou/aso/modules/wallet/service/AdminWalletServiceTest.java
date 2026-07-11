package com.youou.aso.modules.wallet.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.WalletAccount;
import com.youou.aso.modules.wallet.domain.AdminWalletAdjustmentType;
import com.youou.aso.modules.wallet.domain.WalletDirection;
import com.youou.aso.modules.wallet.domain.WalletTransaction;
import com.youou.aso.modules.wallet.domain.WalletTransactionType;
import com.youou.aso.modules.wallet.dto.AdminBalanceAdjustmentCommand;
import com.youou.aso.modules.wallet.dto.AdminRechargeCommand;
import com.youou.aso.modules.wallet.dto.WalletTransactionResult;
import com.youou.aso.modules.wallet.repository.AdminWalletRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminWalletServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-20T00:00:00Z"), ZoneOffset.UTC);

    private final FakeAdminWalletRepository repository = new FakeAdminWalletRepository();
    private final AdminWalletService service = new AdminWalletService(repository, CLOCK);

    @Test
    void rechargeIncreasesBalanceAndWritesCreditTransaction() {
        repository.wallet = wallet("10.00");

        WalletTransactionResult result = service.recharge(new AdminRechargeCommand(10L, new BigDecimal("25.50"), "manual recharge"));

        assertThat(repository.wallet.getBalance()).isEqualByComparingTo("35.50");
        assertThat(result.transactionNo()).matches("WT260620\\d{6}");
        assertThat(result.direction()).isEqualTo(WalletDirection.CREDIT);
        assertThat(result.transactionType()).isEqualTo(WalletTransactionType.ADMIN_RECHARGE);
        assertThat(result.amount()).isEqualByComparingTo("25.50");
        assertThat(result.balanceBefore()).isEqualByComparingTo("10.00");
        assertThat(result.balanceAfter()).isEqualByComparingTo("35.50");
        assertThat(result.remark()).isEqualTo("manual recharge");
    }

    @Test
    void rechargeRejectsNonPositiveAmountWithoutUpdatingWallet() {
        repository.wallet = wallet("10.00");

        assertThatThrownBy(() -> service.recharge(new AdminRechargeCommand(10L, BigDecimal.ZERO, "invalid")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WALLET_AMOUNT_INVALID);
        assertThat(repository.wallet.getBalance()).isEqualByComparingTo("10.00");
        assertThat(repository.savedTransaction).isNull();
    }

    @Test
    void refundAdjustmentIncreasesBalanceAndWritesCreditTransaction() {
        repository.wallet = wallet("10.00");

        WalletTransactionResult result = service.adjustBalance(new AdminBalanceAdjustmentCommand(
                10L,
                AdminWalletAdjustmentType.REFUND,
                new BigDecimal("5.25"),
                " order refund "
        ));

        assertThat(repository.wallet.getBalance()).isEqualByComparingTo("15.25");
        assertThat(result.direction()).isEqualTo(WalletDirection.CREDIT);
        assertThat(result.transactionType()).isEqualTo(WalletTransactionType.ADMIN_REFUND);
        assertThat(result.amount()).isEqualByComparingTo("5.25");
        assertThat(result.balanceBefore()).isEqualByComparingTo("10.00");
        assertThat(result.balanceAfter()).isEqualByComparingTo("15.25");
        assertThat(result.remark()).isEqualTo("order refund");
    }

    @Test
    void giftAdjustmentIncreasesBalanceAndWritesCreditTransaction() {
        repository.wallet = wallet("10.00");

        WalletTransactionResult result = service.adjustBalance(new AdminBalanceAdjustmentCommand(
                10L,
                AdminWalletAdjustmentType.GIFT,
                new BigDecimal("3.00"),
                "promotion gift"
        ));

        assertThat(repository.wallet.getBalance()).isEqualByComparingTo("13.00");
        assertThat(result.direction()).isEqualTo(WalletDirection.CREDIT);
        assertThat(result.transactionType()).isEqualTo(WalletTransactionType.ADMIN_GIFT);
    }

    @Test
    void deductAdjustmentDecreasesBalanceAndWritesDebitTransaction() {
        repository.wallet = wallet("10.00");

        WalletTransactionResult result = service.adjustBalance(new AdminBalanceAdjustmentCommand(
                10L,
                AdminWalletAdjustmentType.DEDUCT,
                new BigDecimal("4.50"),
                "manual correction"
        ));

        assertThat(repository.wallet.getBalance()).isEqualByComparingTo("5.50");
        assertThat(result.direction()).isEqualTo(WalletDirection.DEBIT);
        assertThat(result.transactionType()).isEqualTo(WalletTransactionType.ADMIN_DEDUCT);
        assertThat(result.balanceBefore()).isEqualByComparingTo("10.00");
        assertThat(result.balanceAfter()).isEqualByComparingTo("5.50");
    }

    @Test
    void deductAdjustmentRejectsInsufficientBalanceWithoutUpdatingWallet() {
        repository.wallet = wallet("10.00");

        assertThatThrownBy(() -> service.adjustBalance(new AdminBalanceAdjustmentCommand(
                10L,
                AdminWalletAdjustmentType.DEDUCT,
                new BigDecimal("10.01"),
                "invalid deduct"
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BALANCE_NOT_ENOUGH);
        assertThat(repository.wallet.getBalance()).isEqualByComparingTo("10.00");
        assertThat(repository.savedTransaction).isNull();
    }

    @Test
    void adjustmentRejectsBlankRemarkWithoutUpdatingWallet() {
        repository.wallet = wallet("10.00");

        assertThatThrownBy(() -> service.adjustBalance(new AdminBalanceAdjustmentCommand(
                10L,
                AdminWalletAdjustmentType.GIFT,
                new BigDecimal("1.00"),
                " "
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WALLET_AMOUNT_INVALID);
        assertThat(repository.wallet.getBalance()).isEqualByComparingTo("10.00");
        assertThat(repository.savedTransaction).isNull();
    }

    private WalletAccount wallet(String balance) {
        WalletAccount wallet = new WalletAccount();
        wallet.setId(1L);
        wallet.setCustomerId(10L);
        wallet.setBalance(new BigDecimal(balance));
        wallet.setFrozenBalance(BigDecimal.ZERO);
        wallet.setVersion(0L);
        return wallet;
    }

    private static final class FakeAdminWalletRepository implements AdminWalletRepository {
        private WalletAccount wallet;
        private WalletTransaction savedTransaction;

        @Override
        public Optional<WalletAccount> findByCustomerIdForUpdate(Long customerId) {
            return wallet == null || !customerId.equals(wallet.getCustomerId()) ? Optional.empty() : Optional.of(wallet);
        }

        @Override
        public void updateBalance(Long walletId, BigDecimal balance) {
            wallet.setBalance(balance);
        }

        @Override
        public WalletTransaction saveTransaction(WalletTransaction transaction) {
            transaction.setId(1L);
            savedTransaction = transaction;
            return transaction;
        }
    }
}
