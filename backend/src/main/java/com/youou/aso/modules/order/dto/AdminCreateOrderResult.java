package com.youou.aso.modules.order.dto;

import com.youou.aso.modules.order.domain.AsoOrder;
import com.youou.aso.modules.order.domain.SpecialOrderAudit;

public record AdminCreateOrderResult(
        OrderResult order,
        SpecialOrderAuditResult audit,
        boolean paid,
        boolean waitPayment
) {
    public static AdminCreateOrderResult paid(AsoOrder order) {
        return new AdminCreateOrderResult(OrderResult.from(order), null, true, false);
    }

    public static AdminCreateOrderResult waitPayment(SpecialOrderAudit audit) {
        return new AdminCreateOrderResult(null, SpecialOrderAuditResult.from(audit), false, true);
    }

    public static AdminCreateOrderResult waitPayment(AsoOrder order) {
        return new AdminCreateOrderResult(OrderResult.from(order), null, false, true);
    }
}
