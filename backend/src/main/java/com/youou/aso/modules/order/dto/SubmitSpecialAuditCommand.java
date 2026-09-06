package com.youou.aso.modules.order.dto;

import com.youou.aso.modules.order.domain.OrderType;

import java.util.List;

public record SubmitSpecialAuditCommand(
        Long customerAppId,
        String regionCode,
        OrderType orderType,
        String requestedContent,
        String contactType,
        String contactValue,
        List<AuditItem> items
) {
    public SubmitSpecialAuditCommand(Long customerAppId, OrderType orderType, String requestedContent) {
        this(customerAppId, null, orderType, requestedContent, null, null, null);
    }

    public SubmitSpecialAuditCommand(Long customerAppId, String regionCode, OrderType orderType, String requestedContent) {
        this(customerAppId, regionCode, orderType, requestedContent, null, null, null);
    }

    public SubmitSpecialAuditCommand(
            Long customerAppId,
            String regionCode,
            OrderType orderType,
            String requestedContent,
            List<AuditItem> items
    ) {
        this(customerAppId, regionCode, orderType, requestedContent, null, null, items);
    }

    public record AuditItem(
            String regionCode,
            String keyword,
            String chartType,
            Integer targetRank,
            String coverageNote,
            java.math.BigDecimal unitPrice,
            Integer executionDays
    ) {
        public AuditItem(String regionCode, String keyword, Integer targetRank, String coverageNote) {
            this(regionCode, keyword, null, targetRank, coverageNote, null, null);
        }
        public AuditItem(String regionCode, String keyword, String chartType, Integer targetRank, String coverageNote) {
            this(regionCode, keyword, chartType, targetRank, coverageNote, null, null);
        }
    }
}
