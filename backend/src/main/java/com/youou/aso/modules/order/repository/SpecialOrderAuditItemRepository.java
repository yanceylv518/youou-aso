package com.youou.aso.modules.order.repository;

import com.youou.aso.modules.order.domain.SpecialOrderAuditItem;

import java.util.List;
import java.util.Map;

public interface SpecialOrderAuditItemRepository {
    void saveAll(Long auditId, List<SpecialOrderAuditItem> items);

    Map<Long, List<SpecialOrderAuditItem>> findByAuditIds(List<Long> auditIds);
}
