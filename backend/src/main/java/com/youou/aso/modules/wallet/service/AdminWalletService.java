package com.youou.aso.modules.wallet.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.common.util.BusinessNumberGenerator;
import com.youou.aso.modules.account.domain.WalletAccount;
import com.youou.aso.modules.wallet.domain.AdminWalletAdjustmentType;
import com.youou.aso.modules.wallet.domain.WalletDirection;
import com.youou.aso.modules.wallet.domain.WalletTransaction;
import com.youou.aso.modules.wallet.domain.WalletTransactionType;
import com.youou.aso.modules.wallet.dto.AdminBalanceAdjustmentCommand;
import com.youou.aso.modules.wallet.dto.AdminRechargeCommand;
import com.youou.aso.modules.wallet.dto.WalletTransactionResult;
import com.youou.aso.modules.wallet.repository.AdminWalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class AdminWalletService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private final AdminWalletRepository adminWalletRepository;
    private final Clock clock;

    public AdminWalletService(AdminWalletRepository adminWalletRepository, Clock clock) {
        this.adminWalletRepository = adminWalletRepository;
        this.clock = clock;
    }

    @Transactional
    public WalletTransactionResult recharge(AdminRechargeCommand command) {
        if (command == null || command.customerId() == null || command.amount() == null
                || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.WALLET_AMOUNT_INVALID);
        }
        WalletAccount wallet = adminWalletRepository.findByCustomerIdForUpdate(command.customerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));
        BigDecimal before = wallet.getBalance();
        BigDecimal after = before.add(command.amount());
        adminWalletRepository.updateBalance(wallet.getId(), after);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setCustomerId(wallet.getCustomerId());
        transaction.setWalletId(wallet.getId());
        transaction.setDirection(WalletDirection.CREDIT);
        transaction.setTransactionType(WalletTransactionType.ADMIN_RECHARGE);
        transaction.setAmount(command.amount());
        transaction.setBalanceBefore(before);
        transaction.setBalanceAfter(after);
        transaction.setRelatedOrderId(null);
        transaction.setRemark(command.remark());
        transaction.setCreatedAt(LocalDateTime.ofInstant(clock.instant(), BUSINESS_ZONE));
        return WalletTransactionResult.from(adminWalletRepository.saveTransaction(transaction));
    }

    @Transactional
    public WalletTransactionResult adjustBalance(AdminBalanceAdjustmentCommand command) {
        if (command == null || command.customerId() == null || command.adjustmentType() == null
                || command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) <= 0
                || command.remark() == null || command.remark().isBlank()) {
            throw new BusinessException(ErrorCode.WALLET_AMOUNT_INVALID);
        }
        WalletAccount wallet = adminWalletRepository.findByCustomerIdForUpdate(command.customerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));
        BigDecimal before = wallet.getBalance();
        WalletDirection direction = directionOf(command.adjustmentType());
        BigDecimal after = direction == WalletDirection.CREDIT
                ? before.add(command.amount())
                : before.subtract(command.amount());
        if (after.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.BALANCE_NOT_ENOUGH);
        }
        adminWalletRepository.updateBalance(wallet.getId(), after);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setCustomerId(wallet.getCustomerId());
        transaction.setWalletId(wallet.getId());
        transaction.setDirection(direction);
        transaction.setTransactionType(transactionTypeOf(command.adjustmentType()));
        transaction.setAmount(command.amount());
        transaction.setBalanceBefore(before);
        transaction.setBalanceAfter(after);
        transaction.setRelatedOrderId(null);
        transaction.setRemark(command.remark().trim());
        transaction.setCreatedAt(LocalDateTime.ofInstant(clock.instant(), BUSINESS_ZONE));
        return WalletTransactionResult.from(adminWalletRepository.saveTransaction(transaction));
    }

    private WalletDirection directionOf(AdminWalletAdjustmentType adjustmentType) {
        return adjustmentType == AdminWalletAdjustmentType.DEDUCT ? WalletDirection.DEBIT : WalletDirection.CREDIT;
    }

    private WalletTransactionType transactionTypeOf(AdminWalletAdjustmentType adjustmentType) {
        return switch (adjustmentType) {
            case REFUND -> WalletTransactionType.ADMIN_REFUND;
            case GIFT -> WalletTransactionType.ADMIN_GIFT;
            case DEDUCT -> WalletTransactionType.ADMIN_DEDUCT;
        };
    }

    private String generateTransactionNo() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), BUSINESS_ZONE);
        return BusinessNumberGenerator.generate("WT", now);
    }
}
