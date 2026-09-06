package com.youou.aso.modules.order.dto;

import java.math.BigDecimal;

public record ReviewSpecialAuditCommand(
        String negotiatedContent,
        BigDecimal negotiatedPrice,
        java.util.List<ItemPricing> itemPricing
) {
    public ReviewSpecialAuditCommand(String negotiatedContent, BigDecimal negotiatedPrice) {
        this(negotiatedContent, negotiatedPrice, null);
    }
    public record ItemPricing(Long itemId, BigDecimal unitPrice, Integer executionDays) {}
}
