package com.youou.aso.modules.order.dto;

import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.order.domain.SpecialAuditStatus;
import com.youou.aso.modules.order.domain.SpecialOrderAudit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SpecialOrderAuditResult(
        Long id,
        String auditNo,
        Long customerId,
        Long customerAppId,
        OrderType orderType,
        StoreType storeType,
        String regionCode,
        String appIdentifier,
        String appName,
        String appIconUrl,
        String requestedContent,
        String contactType,
        String contactValue,
        String negotiatedContent,
        BigDecimal negotiatedPrice,
        SpecialAuditStatus status,
        Long reviewedByAdminId,
        LocalDateTime reviewedAt,
        String cancelReason,
        Long submittedOrderId,
        LocalDateTime submittedAt,
        LocalDateTime createdAt,
        java.util.List<SpecialOrderAuditItemResult> items
) {
    public static SpecialOrderAuditResult from(SpecialOrderAudit audit) {
        return new SpecialOrderAuditResult(
                audit.getId(),
                audit.getAuditNo(),
                audit.getCustomerId(),
                audit.getCustomerAppId(),
                audit.getOrderType(),
                audit.getStoreType(),
                audit.getRegionCode(),
                audit.getAppIdentifier(),
                audit.getAppName(),
                audit.getAppIconUrl(),
                audit.getRequestedContent(),
                audit.getContactType(),
                audit.getContactValue(),
                audit.getNegotiatedContent(),
                audit.getNegotiatedPrice(),
                audit.getStatus(),
                audit.getReviewedByAdminId(),
                audit.getReviewedAt(),
                audit.getCancelReason(),
                audit.getSubmittedOrderId(),
                audit.getSubmittedAt(),
                audit.getCreatedAt(),
                audit.getItems().stream().map(SpecialOrderAuditItemResult::from).toList()
        );
    }
}
