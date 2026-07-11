package com.youou.aso.modules.order.dto;

import com.youou.aso.modules.order.domain.SpecialOrderAuditItem;

public record SpecialOrderAuditItemResult(
        Long id,
        String regionCode,
        String keyword,
        Integer targetRank,
        String coverageNote
) {
    public static SpecialOrderAuditItemResult from(SpecialOrderAuditItem item) {
        return new SpecialOrderAuditItemResult(
                item.getId(),
                item.getRegionCode(),
                item.getKeyword(),
                item.getTargetRank(),
                item.getCoverageNote()
        );
    }
}
