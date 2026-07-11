package com.youou.aso.modules.order.domain;

import com.youou.aso.modules.appmanagement.domain.StoreType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SpecialOrderAudit {
    private Long id;
    private String auditNo;
    private Long customerId;
    private Long customerAppId;
    private OrderType orderType;
    private StoreType storeType;
    private String regionCode;
    private String appIdentifier;
    private String appName;
    private String appIconUrl;
    private String requestedContent;
    private String contactType;
    private String contactValue;
    private String negotiatedContent;
    private BigDecimal negotiatedPrice;
    private SpecialAuditStatus status;
    private Long reviewedByAdminId;
    private LocalDateTime reviewedAt;
    private String cancelReason;
    private Long submittedOrderId;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SpecialOrderAuditItem> items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuditNo() {
        return auditNo;
    }

    public void setAuditNo(String auditNo) {
        this.auditNo = auditNo;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getCustomerAppId() {
        return customerAppId;
    }

    public void setCustomerAppId(Long customerAppId) {
        this.customerAppId = customerAppId;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public StoreType getStoreType() {
        return storeType;
    }

    public void setStoreType(StoreType storeType) {
        this.storeType = storeType;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getAppIdentifier() {
        return appIdentifier;
    }

    public void setAppIdentifier(String appIdentifier) {
        this.appIdentifier = appIdentifier;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppIconUrl() {
        return appIconUrl;
    }

    public void setAppIconUrl(String appIconUrl) {
        this.appIconUrl = appIconUrl;
    }

    public String getRequestedContent() {
        return requestedContent;
    }

    public void setRequestedContent(String requestedContent) {
        this.requestedContent = requestedContent;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public String getContactValue() {
        return contactValue;
    }

    public void setContactValue(String contactValue) {
        this.contactValue = contactValue;
    }

    public String getNegotiatedContent() {
        return negotiatedContent;
    }

    public void setNegotiatedContent(String negotiatedContent) {
        this.negotiatedContent = negotiatedContent;
    }

    public BigDecimal getNegotiatedPrice() {
        return negotiatedPrice;
    }

    public void setNegotiatedPrice(BigDecimal negotiatedPrice) {
        this.negotiatedPrice = negotiatedPrice;
    }

    public SpecialAuditStatus getStatus() {
        return status;
    }

    public void setStatus(SpecialAuditStatus status) {
        this.status = status;
    }

    public Long getReviewedByAdminId() {
        return reviewedByAdminId;
    }

    public void setReviewedByAdminId(Long reviewedByAdminId) {
        this.reviewedByAdminId = reviewedByAdminId;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Long getSubmittedOrderId() {
        return submittedOrderId;
    }

    public void setSubmittedOrderId(Long submittedOrderId) {
        this.submittedOrderId = submittedOrderId;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<SpecialOrderAuditItem> getItems() {
        return items;
    }

    public void setItems(List<SpecialOrderAuditItem> items) {
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    }
}
