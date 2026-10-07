package com.youou.aso.modules.pricing.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.pricing.domain.OrderModuleConfig;
import com.youou.aso.modules.pricing.repository.OrderModuleConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class OrderModuleConfigService {
    private final OrderModuleConfigRepository repository;
    public OrderModuleConfigService(OrderModuleConfigRepository repository) { this.repository = repository; }
    public List<OrderModuleConfig> listAll() { return repository.findAll(); }
    public List<OrderModuleConfig> listEnabled(OrderType type) {
        if (type != null) return repository.findEnabled(type);
        return repository.findAll().stream()
                .filter(OrderModuleConfig::enabled)
                .sorted(Comparator.comparingInt(OrderModuleConfig::sortOrder)
                        .thenComparing(OrderModuleConfig::id, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }
    public OrderModuleConfig requireEnabled(Long id, OrderType type) {
        OrderModuleConfig module = repository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.PRICE_INVALID));
        if (!module.enabled() || module.orderType() != type) throw new BusinessException(ErrorCode.PRICE_INVALID);
        return module;
    }
    @Transactional
    public OrderModuleConfig save(Long id, String name, String nameEn, String nameRu, String namePt, String nameEs,
                                  String description, String descriptionEn, String descriptionRu, String descriptionPt, String descriptionEs,
                                  OrderType type, BigDecimal price, BigDecimal chinaPrice, boolean enabled, int sort) {
        return save(id,name,nameEn,nameRu,namePt,nameEs,description,descriptionEn,descriptionRu,descriptionPt,descriptionEs,type,price,chinaPrice,enabled,sort,List.of(com.youou.aso.modules.appmanagement.domain.StoreType.values()));
    }
    @Transactional
    public OrderModuleConfig save(Long id, String name, String nameEn, String nameRu, String namePt, String nameEs,
                                  String description, String descriptionEn, String descriptionRu, String descriptionPt, String descriptionEs,
                                  OrderType type, BigDecimal price, BigDecimal chinaPrice, boolean enabled, int sort,
                                  List<com.youou.aso.modules.appmanagement.domain.StoreType> storeTypes) {
        if (storeTypes == null || storeTypes.isEmpty() || storeTypes.stream().anyMatch(java.util.Objects::isNull)) throw new BusinessException(ErrorCode.BAD_REQUEST);
        boolean audited = type == OrderType.RANK_GUARANTEE || type == OrderType.CHART_RANK_GUARANTEE
                || type == OrderType.KEYWORD_COVERAGE;
        if (isBlank(name) || isBlank(nameEn) || isBlank(nameRu) || isBlank(namePt) || isBlank(nameEs)
                || isBlank(description) || isBlank(descriptionEn) || isBlank(descriptionRu) || isBlank(descriptionPt) || isBlank(descriptionEs)
                || type == null
                || (!audited && (price == null || chinaPrice == null || price.signum() <= 0 || chinaPrice.signum() <= 0))
                || (price != null && price.signum() < 0) || (chinaPrice != null && chinaPrice.signum() < 0)) {
            throw new BusinessException(ErrorCode.PRICE_INVALID);
        }
        if (audited) { price = null; chinaPrice = null; }
        if (id != null && repository.findById(id).isEmpty()) throw new BusinessException(ErrorCode.PRICE_INVALID);
        return repository.save(new OrderModuleConfig(id, name.trim(), nameEn.trim(), nameRu.trim(), namePt.trim(), nameEs.trim(),
                description.trim(), descriptionEn.trim(), descriptionRu.trim(), descriptionPt.trim(), descriptionEs.trim(),
                type, price, chinaPrice, enabled, sort, storeTypes.stream().distinct().toList()));
    }
    private boolean isBlank(String value) { return value == null || value.isBlank(); }
    @Transactional public void delete(Long id) { repository.deleteById(id); }
}
