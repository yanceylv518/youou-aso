package com.youou.aso.modules.order.dto;

import com.youou.aso.modules.order.domain.SpecialOrderAuditItem;

public record SpecialOrderAuditItemResult(
        Long id,
        String regionCode,
        String keyword,
        String chartType,
        Integer targetRank,
        String coverageNote,
        java.math.BigDecimal unitPrice,
        Integer executionDays,
        java.math.BigDecimal amount
) {
    public static SpecialOrderAuditItemResult from(SpecialOrderAuditItem item) {
        return new SpecialOrderAuditItemResult(
                item.getId(),
                item.getRegionCode(),
                item.getKeyword(),
                item.getChartType(),
                item.getTargetRank(),
                item.getCoverageNote(),
                item.getUnitPrice(),
                item.getExecutionDays(),
                item.getUnitPrice() == null || item.getExecutionDays() == null ? null
                        : item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getExecutionDays()))
        );
    }
}
