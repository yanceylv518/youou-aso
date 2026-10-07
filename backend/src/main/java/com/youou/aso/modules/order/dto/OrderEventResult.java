package com.youou.aso.modules.order.dto;

import com.youou.aso.modules.order.domain.OrderEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderEventResult(
        Long id,
        String eventType,
        Integer quantityBefore,
        Integer quantityAfter,
        Integer completedBefore,
        Integer completedAfter,
        BigDecimal amountBefore,
        BigDecimal amountAfter,
        Long createdByAdminId,
        LocalDateTime createdAt,
        String reason
) {
    public static OrderEventResult from(OrderEvent event) {
        return new OrderEventResult(
                event.getId(),
                event.getEventType(),
                event.getQuantityBefore(),
                event.getQuantityAfter(),
                event.getCompletedBefore(),
                event.getCompletedAfter(),
                event.getAmountBefore(),
                event.getAmountAfter(),
                event.getCreatedByAdminId(),
                event.getCreatedAt(),
                event.getReason()
        );
    }
}