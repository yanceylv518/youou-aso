package com.youou.aso.modules.order.domain;

import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.pricing.domain.PriceCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AsoOrder {
    private Long id;
    private String orderNo;
    private Long customerId;
    private String customerUsername;
    private String customerEmail;
    private Long customerAppId;
    private Long sourceAuditId;
    private OrderType orderType;
    private PriceCode pricingCode;
    private StoreType storeType;
    private String regionCode;
    private String appIdentifier;
    private String appName;
    private String appIconUrl;
    private OrderStatus status;
    private LocalDate orderStartDate;
    private LocalDate orderEndDate;
    private Integer executionHours;
    private Integer totalDays;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private Long deductedTransactionId;
    private Long refundTransactionId;
    private String rejectReason;
    private Long confirmedByAdminId;
    private LocalDateTime confirmedAt;
    private Long executedByAdminId;
    private LocalDateTime executedAt;
    private LocalDateTime expectedCompletedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItem> items = new ArrayList<>();
    private List<OrderCommentDetail> commentDetails = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerUsername() {
        return customerUsername;
    }

    public void setCustomerUsername(String customerUsername) {
        this.customerUsername = customerUsername;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public Long getCustomerAppId() {
        return customerAppId;
    }

    public void setCustomerAppId(Long customerAppId) {
        this.customerAppId = customerAppId;
    }

    public Long getSourceAuditId() {
        return sourceAuditId;
    }

    public void setSourceAuditId(Long sourceAuditId) {
        this.sourceAuditId = sourceAuditId;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public PriceCode getPricingCode() {
        return pricingCode;
    }

    public void setPricingCode(PriceCode pricingCode) {
        this.pricingCode = pricingCode;
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

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDate getOrderStartDate() {
        return orderStartDate;
    }

    public void setOrderStartDate(LocalDate orderStartDate) {
        this.orderStartDate = orderStartDate;
    }

    public LocalDate getOrderEndDate() {
        return orderEndDate;
    }

    public void setOrderEndDate(LocalDate orderEndDate) {
        this.orderEndDate = orderEndDate;
    }

    public Integer getExecutionHours() {
        return executionHours;
    }

    public void setExecutionHours(Integer executionHours) {
        this.executionHours = executionHours;
    }

    public Integer getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(Integer totalDays) {
        this.totalDays = totalDays;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(BigDecimal balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public Long getDeductedTransactionId() {
        return deductedTransactionId;
    }

    public void setDeductedTransactionId(Long deductedTransactionId) {
        this.deductedTransactionId = deductedTransactionId;
    }

    public Long getRefundTransactionId() {
        return refundTransactionId;
    }

    public void setRefundTransactionId(Long refundTransactionId) {
        this.refundTransactionId = refundTransactionId;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Long getConfirmedByAdminId() {
        return confirmedByAdminId;
    }

    public void setConfirmedByAdminId(Long confirmedByAdminId) {
        this.confirmedByAdminId = confirmedByAdminId;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Long getExecutedByAdminId() {
        return executedByAdminId;
    }

    public void setExecutedByAdminId(Long executedByAdminId) {
        this.executedByAdminId = executedByAdminId;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    public LocalDateTime getExpectedCompletedAt() {
        return expectedCompletedAt;
    }

    public void setExpectedCompletedAt(LocalDateTime expectedCompletedAt) {
        this.expectedCompletedAt = expectedCompletedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
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

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    }

    public List<OrderCommentDetail> getCommentDetails() {
        return commentDetails;
    }

    public void setCommentDetails(List<OrderCommentDetail> commentDetails) {
        this.commentDetails = commentDetails == null ? new ArrayList<>() : new ArrayList<>(commentDetails);
    }
}
