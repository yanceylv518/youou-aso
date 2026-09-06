package com.youou.aso.modules.wallet.domain;

import java.time.LocalDateTime;

public class WalletTransactionTypeConfig {
    private WalletTransactionType transactionType;
    private String displayNameZh;
    private String displayNameEn;
    private String displayNameRu;
    private String displayNamePt;
    private String displayNameEs;
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
        config.setDisplayNameRu(defaultRu(transactionType));
        config.setDisplayNamePt(defaultPt(transactionType));
        config.setDisplayNameEs(defaultEs(transactionType));
        return config;
    }

    private static String defaultRu(WalletTransactionType type) { return switch (type) { case ORDER_DEDUCT -> "Списание по заказу"; case ORDER_REFUND -> "Возврат по заказу"; case ADMIN_RECHARGE -> "Пополнение"; case ADMIN_ADJUSTMENT -> "Корректировка"; case ADMIN_REFUND -> "Возврат администратора"; case ADMIN_GIFT -> "Бонусное зачисление"; case ADMIN_DEDUCT -> "Корректирующее списание"; case DELIVERY -> "Выполнение заказа"; }; }
    private static String defaultPt(WalletTransactionType type) { return switch (type) { case ORDER_DEDUCT -> "Débito da encomenda"; case ORDER_REFUND -> "Reembolso da encomenda"; case ADMIN_RECHARGE -> "Carregamento"; case ADMIN_ADJUSTMENT -> "Ajuste"; case ADMIN_REFUND -> "Reembolso administrativo"; case ADMIN_GIFT -> "Crédito de oferta"; case ADMIN_DEDUCT -> "Débito de ajuste"; case DELIVERY -> "Execução da encomenda"; }; }
    private static String defaultEs(WalletTransactionType type) { return switch (type) { case ORDER_DEDUCT -> "Cargo del pedido"; case ORDER_REFUND -> "Reembolso del pedido"; case ADMIN_RECHARGE -> "Recarga"; case ADMIN_ADJUSTMENT -> "Ajuste"; case ADMIN_REFUND -> "Reembolso administrativo"; case ADMIN_GIFT -> "Abono promocional"; case ADMIN_DEDUCT -> "Cargo de ajuste"; case DELIVERY -> "Ejecución del pedido"; }; }

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

    public String getDisplayNameRu() { return displayNameRu; }
    public void setDisplayNameRu(String value) { this.displayNameRu = value; }
    public String getDisplayNamePt() { return displayNamePt; }
    public void setDisplayNamePt(String value) { this.displayNamePt = value; }
    public String getDisplayNameEs() { return displayNameEs; }
    public void setDisplayNameEs(String value) { this.displayNameEs = value; }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
