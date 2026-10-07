package com.youou.aso.modules.order.repository;

import com.youou.aso.modules.order.domain.SpecialOrderAudit;

import java.util.List;
import java.util.Optional;

public interface SpecialOrderAuditRepository {
    SpecialOrderAudit save(SpecialOrderAudit audit);

    Optional<SpecialOrderAudit> findById(Long id);

    default Optional<SpecialOrderAudit> findByIdForUpdate(Long id) { return findById(id); }

    SpecialOrderAudit update(SpecialOrderAudit audit);

    List<SpecialOrderAudit> findByCustomerId(Long customerId);

    List<SpecialOrderAudit> findAll();

    default List<SpecialOrderAudit> findByCustomerId(Long customerId, int limit, int offset) {
        return findByCustomerId(customerId).stream().skip(offset).limit(limit).toList();
    }

    default long countByCustomerId(Long customerId) {
        return findByCustomerId(customerId).size();
    }

    default List<SpecialOrderAudit> findAll(int limit, int offset) {
        return findAll().stream().skip(offset).limit(limit).toList();
    }

    default long countAll() {
        return findAll().size();
    }
}
