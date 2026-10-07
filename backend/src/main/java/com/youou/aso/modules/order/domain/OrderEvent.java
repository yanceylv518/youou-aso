package com.youou.aso.modules.order.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderEvent {
    private String reason;
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    private Long id;
    private Long orderId;
    private String eventType;
    private Integer quantityBefore;
    private Integer quantityAfter;
    private Integer completedBefore;
    private Integer completedAfter;
    private BigDecimal amountBefore;
    private BigDecimal amountAfter;
    private Long createdByAdminId;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Integer getQuantityBefore() { return quantityBefore; }
    public void setQuantityBefore(Integer quantityBefore) { this.quantityBefore = quantityBefore; }
    public Integer getQuantityAfter() { return quantityAfter; }
    public void setQuantityAfter(Integer quantityAfter) { this.quantityAfter = quantityAfter; }
    public Integer getCompletedBefore() { return completedBefore; }
    public void setCompletedBefore(Integer completedBefore) { this.completedBefore = completedBefore; }
    public Integer getCompletedAfter() { return completedAfter; }
    public void setCompletedAfter(Integer completedAfter) { this.completedAfter = completedAfter; }
    public BigDecimal getAmountBefore() { return amountBefore; }
    public void setAmountBefore(BigDecimal amountBefore) { this.amountBefore = amountBefore; }
    public BigDecimal getAmountAfter() { return amountAfter; }
    public void setAmountAfter(BigDecimal amountAfter) { this.amountAfter = amountAfter; }
    public Long getCreatedByAdminId() { return createdByAdminId; }
    public void setCreatedByAdminId(Long createdByAdminId) { this.createdByAdminId = createdByAdminId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}