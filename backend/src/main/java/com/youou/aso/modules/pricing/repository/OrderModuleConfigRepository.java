package com.youou.aso.modules.pricing.repository;

import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.pricing.domain.OrderModuleConfig;
import java.util.List;
import java.util.Optional;

public interface OrderModuleConfigRepository {
    List<OrderModuleConfig> findAll();
    List<OrderModuleConfig> findEnabled(OrderType orderType);
    Optional<OrderModuleConfig> findById(Long id);
    OrderModuleConfig save(OrderModuleConfig module);
    void deleteById(Long id);
}
