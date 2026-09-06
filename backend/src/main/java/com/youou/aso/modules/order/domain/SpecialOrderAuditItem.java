package com.youou.aso.modules.order.domain;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class SpecialOrderAuditItem {
    private Long id;
    private Long auditId;
    private String regionCode;
    private String keyword;
    private String chartType;
    private Integer targetRank;
    private String coverageNote;
    private BigDecimal unitPrice;
    private Integer executionDays;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getChartType() {
        return chartType;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
    }

    public Integer getTargetRank() {
        return targetRank;
    }

    public void setTargetRank(Integer targetRank) {
        this.targetRank = targetRank;
    }

    public String getCoverageNote() {
        return coverageNote;
    }

    public void setCoverageNote(String coverageNote) {
        this.coverageNote = coverageNote;
    }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public Integer getExecutionDays() { return executionDays; }
    public void setExecutionDays(Integer executionDays) { this.executionDays = executionDays; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
