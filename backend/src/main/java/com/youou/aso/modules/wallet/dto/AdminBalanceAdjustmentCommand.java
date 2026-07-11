package com.youou.aso.modules.wallet.dto;

import com.youou.aso.modules.wallet.domain.AdminWalletAdjustmentType;

import java.math.BigDecimal;

public record AdminBalanceAdjustmentCommand(
        Long customerId,
        AdminWalletAdjustmentType adjustmentType,
        BigDecimal amount,
        String remark
) {
}
