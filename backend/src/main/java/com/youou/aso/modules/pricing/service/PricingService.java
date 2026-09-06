package com.youou.aso.modules.pricing.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.pricing.domain.PriceCode;
import com.youou.aso.modules.pricing.domain.PricingConfig;
import com.youou.aso.modules.pricing.dto.PricingConfigResult;
import com.youou.aso.modules.pricing.dto.OrderTypeRegionPricingResult;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.pricing.dto.UpdatePricingCommand;
import com.youou.aso.modules.pricing.repository.PricingConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;

@Service
public class PricingService {
    private final PricingConfigRepository pricingConfigRepository;

    public PricingService(PricingConfigRepository pricingConfigRepository) {
        this.pricingConfigRepository = pricingConfigRepository;
    }

    public List<PricingConfig> listPricing() {
        Map<PriceCode, PricingConfig> existing = new EnumMap<>(PriceCode.class);
        pricingConfigRepository.findAll().forEach(config -> existing.put(config.getCode(), config));
        return List.of(PriceCode.values()).stream()
                .map(code -> existing.getOrDefault(code, PricingConfig.enabled(code, BigDecimal.ZERO)))
                .sorted(Comparator.comparingInt(config -> config.getCode().ordinal()))
                .toList();
    }

    public List<PricingConfigResult> listPricingResults() {
        return listPricing().stream()
                .map(config -> new PricingConfigResult(config.getCode(), config.getUnitPrice(), config.getChinaUnitPrice(), config.isEnabled()))
                .toList();
    }

    @Transactional
    public List<PricingConfigResult> updatePricing(List<UpdatePricingCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new BusinessException(ErrorCode.PRICE_INVALID);
        }

        Map<PriceCode, BigDecimal> requestedPrices = new EnumMap<>(PriceCode.class);
        Map<PriceCode, BigDecimal> requestedChinaPrices = new EnumMap<>(PriceCode.class);
        for (UpdatePricingCommand command : commands) {
            if (command == null || command.code() == null || command.unitPrice() == null
                    || command.unitPrice().compareTo(BigDecimal.ZERO) < 0 || command.chinaUnitPrice() == null
                    || command.chinaUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(ErrorCode.PRICE_INVALID);
            }
            requestedPrices.put(command.code(), command.unitPrice());
            requestedChinaPrices.put(command.code(), command.chinaUnitPrice());
        }

        List<PricingConfig> updated = listPricing().stream()
                .map(config -> {
                    BigDecimal unitPrice = requestedPrices.get(config.getCode());
                    if (unitPrice == null) return config;
                    PricingConfig next = PricingConfig.enabled(config.getCode(), unitPrice);
                    next.setChinaUnitPrice(requestedChinaPrices.get(config.getCode()));
                    return next;
                })
                .toList();
        pricingConfigRepository.saveAll(updated);
        return updated.stream()
                .map(config -> new PricingConfigResult(config.getCode(), config.getUnitPrice(), config.getChinaUnitPrice(), config.isEnabled()))
                .toList();
    }

    public List<OrderTypeRegionPricingResult> listOrderTypeRegionPricing() {
        return List.of(OrderType.values()).stream().map(orderType -> {
            Map<PriceCode, Map<String, BigDecimal>> prices = new LinkedHashMap<>();
            priceCodes(orderType).forEach(code -> prices.put(code, pricingConfigRepository.findRegionPrices(code)));
            return new OrderTypeRegionPricingResult(orderType, pricingConfigRepository.findAllowedRegionCodes(orderType), prices);
        }).toList();
    }

    @Transactional
    public List<OrderTypeRegionPricingResult> updateOrderTypeRegionPricing(List<OrderTypeRegionPricingResult> configs) {
        if (configs == null || configs.isEmpty()) throw new BusinessException(ErrorCode.BAD_REQUEST);
        for (OrderTypeRegionPricingResult config : configs) {
            if (config == null || config.orderType() == null || config.allowedRegionCodes() == null || config.allowedRegionCodes().isEmpty()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST);
            }
            List<String> regions = config.allowedRegionCodes().stream().filter(code -> code != null && !code.isBlank())
                    .map(code -> code.trim().toUpperCase()).distinct().toList();
            pricingConfigRepository.replaceAllowedRegions(config.orderType(), regions);
            Map<PriceCode, Map<String, BigDecimal>> requested = config.regionPrices() == null ? Map.of() : config.regionPrices();
            for (PriceCode priceCode : priceCodes(config.orderType())) {
                Map<String, BigDecimal> valid = new LinkedHashMap<>();
                requested.getOrDefault(priceCode, Map.of()).forEach((region, price) -> {
                    String normalized = region == null ? "" : region.trim().toUpperCase();
                    if (!normalized.isBlank() && regions.contains(normalized) && price != null && price.signum() >= 0) valid.put(normalized, price);
                });
                pricingConfigRepository.replaceRegionPrices(priceCode, valid);
            }
        }
        return listOrderTypeRegionPricing();
    }

    private Set<PriceCode> priceCodes(OrderType orderType) {
        return switch (orderType) {
            case KEYWORD_INSTALL -> Set.of(PriceCode.KEYWORD_INSTALL);
            case DOWNLOAD -> Set.of(PriceCode.DOWNLOAD);
            case RATING -> Set.of(PriceCode.RATING_5, PriceCode.RATING_4);
            case REVIEW -> Set.of(PriceCode.REVIEW_5, PriceCode.REVIEW_4);
            default -> Set.of();
        };
    }
}
