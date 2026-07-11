package com.youou.aso.modules.wallet.dto;

import java.math.BigDecimal;

public record AdminRechargeCommand(
        Long customerId,
        BigDecimal amount,
        String remark
) {
}
