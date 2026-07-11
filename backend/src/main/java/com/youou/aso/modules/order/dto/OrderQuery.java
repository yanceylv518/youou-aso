package com.youou.aso.modules.order.dto;

import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.order.domain.OrderStatus;
import com.youou.aso.modules.order.domain.OrderType;

import java.time.LocalDate;

public record OrderQuery(
        StoreType storeType,
        OrderStatus status,
        String keyword,
        Long customerId,
        Long customerAppId,
        String regionCode,
        OrderType orderType,
        Boolean specialOrder,
        LocalDate orderDateFrom,
        LocalDate orderDateTo,
        LocalDate createdDateFrom,
        LocalDate createdDateTo
) {
    public OrderQuery(StoreType storeType, OrderStatus status) {
        this(storeType, status, null, null, null, null, null, null, null, null, null, null);
    }
}
