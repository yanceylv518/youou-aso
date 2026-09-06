package com.youou.aso.modules.pricing.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PricingConfig {
    private PriceCode code;
    private BigDecimal unitPrice;
    private BigDecimal chinaUnitPrice;
    private boolean enabled;
    private LocalDateTime updatedAt;

    public static PricingConfig enabled(PriceCode code, BigDecimal unitPrice) {
        PricingConfig config = new PricingConfig();
        config.setCode(code);
        config.setUnitPrice(unitPrice);
        config.setChinaUnitPrice(unitPrice);
        config.setEnabled(true);
        return config;
    }

    public PriceCode getCode() {
        return code;
    }

    public void setCode(PriceCode code) {
        this.code = code;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getChinaUnitPrice() {
        return chinaUnitPrice;
    }

    public void setChinaUnitPrice(BigDecimal chinaUnitPrice) {
        this.chinaUnitPrice = chinaUnitPrice;
    }
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
