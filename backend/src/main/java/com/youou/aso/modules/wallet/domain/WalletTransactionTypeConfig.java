package com.youou.aso.modules.wallet.domain;

import java.time.LocalDateTime;

public class WalletTransactionTypeConfig {
    private WalletTransactionType transactionType;
    private String displayNameZh;
    private String displayNameEn;
    private LocalDateTime updatedAt;

    public static WalletTransactionTypeConfig defaultFor(WalletTransactionType transactionType) {
        WalletTransactionTypeConfig config = new WalletTransactionTypeConfig();
        config.setTransactionType(transactionType);
        switch (transactionType) {
            case ORDER_DEDUCT -> {
                config.setDisplayNameZh("订单扣款");
                config.setDisplayNameEn("Order deduction");
            }
            case ORDER_REFUND -> {
                config.setDisplayNameZh("订单退款");
                config.setDisplayNameEn("Order refund");
            }
            case ADMIN_RECHARGE -> {
                config.setDisplayNameZh("充值");
                config.setDisplayNameEn("Recharge");
            }
            case ADMIN_ADJUSTMENT -> {
                config.setDisplayNameZh("划扣");
                config.setDisplayNameEn("Deduction");
            }
            case ADMIN_REFUND -> {
                config.setDisplayNameZh("管理员退款");
                config.setDisplayNameEn("Admin refund");
            }
            case ADMIN_GIFT -> {
                config.setDisplayNameZh("赠送余额");
                config.setDisplayNameEn("Gift credit");
            }
            case ADMIN_DEDUCT -> {
                config.setDisplayNameZh("余额扣减");
                config.setDisplayNameEn("Balance deduction");
            }
            case DELIVERY -> {
                config.setDisplayNameZh("配送");
                config.setDisplayNameEn("Delivery");
            }
        }
        return config;
    }

    public WalletTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(WalletTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getDisplayNameZh() {
        return displayNameZh;
    }

    public void setDisplayNameZh(String displayNameZh) {
        this.displayNameZh = displayNameZh;
    }

    public String getDisplayNameEn() {
        return displayNameEn;
    }

    public void setDisplayNameEn(String displayNameEn) {
        this.displayNameEn = displayNameEn;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
