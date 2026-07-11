package com.youou.aso.modules.order.dto;

import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.order.domain.AsoOrder;
import com.youou.aso.modules.order.domain.OrderStatus;
import com.youou.aso.modules.order.domain.OrderType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResult(
        Long id,
        String orderNo,
        Long customerId,
        String customerUsername,
        String customerEmail,
        Long customerAppId,
        Long sourceAuditId,
        OrderType orderType,
        StoreType storeType,
        String regionCode,
        String appIdentifier,
        String appName,
        String appIconUrl,
        OrderStatus status,
        LocalDate orderStartDate,
        LocalDate orderEndDate,
        Integer executionHours,
        Integer totalDays,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        LocalDateTime expectedCompletedAt,
        LocalDateTime confirmedAt,
        LocalDateTime executedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        List<OrderItemResult> items,
        List<OrderCommentDetailResult> commentDetails
) {
    public static OrderResult from(AsoOrder order) {
        return new OrderResult(
                order.getId(),
                order.getOrderNo(),
                order.getCustomerId(),
                order.getCustomerUsername(),
                order.getCustomerEmail(),
                order.getCustomerAppId(),
                order.getSourceAuditId(),
                order.getOrderType(),
                order.getStoreType(),
                order.getRegionCode(),
                order.getAppIdentifier(),
                order.getAppName(),
                order.getAppIconUrl(),
                order.getStatus(),
                order.getOrderStartDate(),
                order.getOrderEndDate(),
                order.getExecutionHours(),
                order.getTotalDays(),
                order.getQuantity(),
                order.getUnitPrice(),
                order.getTotalAmount(),
                order.getExpectedCompletedAt(),
                order.getConfirmedAt(),
                order.getExecutedAt(),
                order.getCompletedAt(),
                order.getCreatedAt(),
                order.getItems().stream().map(OrderItemResult::from).toList(),
                order.getCommentDetails().stream().map(OrderCommentDetailResult::from).toList()
        );
    }
}
