package com.youou.aso.modules.order.dto;

import com.youou.aso.modules.order.domain.OrderItem;

import java.math.BigDecimal;

public record OrderItemResult(
        Long id,
        String itemType,
        String itemName,
        String regionCode,
        Integer quantity,
        Integer completedQuantity,
        BigDecimal unitPrice,
        BigDecimal amount,
        String metadataJson
) {
    public static OrderItemResult from(OrderItem item) {
        return new OrderItemResult(
                item.getId(),
                item.getItemType(),
                item.getItemName(),
                item.getRegionCode(),
                item.getQuantity(),
                item.getCompletedQuantity(),
                item.getUnitPrice(),
                item.getAmount(),
                item.getMetadataJson()
        );
    }
}
