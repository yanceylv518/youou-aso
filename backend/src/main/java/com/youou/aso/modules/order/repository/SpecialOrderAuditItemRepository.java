package com.youou.aso.modules.order.repository;

import com.youou.aso.modules.order.domain.SpecialOrderAuditItem;

import java.util.List;
import java.util.Map;

public interface SpecialOrderAuditItemRepository {
    void saveAll(Long auditId, List<SpecialOrderAuditItem> items);

    void deleteByAuditId(Long auditId);

    Map<Long, List<SpecialOrderAuditItem>> findByAuditIds(List<Long> auditIds);

    void updatePricing(Long id, java.math.BigDecimal unitPrice, Integer executionDays);
}
