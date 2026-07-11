package com.youou.aso.modules.order.dto;

import com.youou.aso.modules.order.domain.OrderItem;

import java.math.BigDecimal;

public record OrderItemResult(
        String itemType,
        String itemName,
        String regionCode,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal amount,
        String metadataJson
) {
    public static OrderItemResult from(OrderItem item) {
        return new OrderItemResult(
                item.getItemType(),
                item.getItemName(),
                item.getRegionCode(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getAmount(),
                item.getMetadataJson()
        );
    }
}
