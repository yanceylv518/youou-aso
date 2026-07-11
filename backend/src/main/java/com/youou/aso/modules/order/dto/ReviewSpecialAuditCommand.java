package com.youou.aso.modules.order.dto;

import java.math.BigDecimal;

public record ReviewSpecialAuditCommand(
        String negotiatedContent,
        BigDecimal negotiatedPrice
) {
}
