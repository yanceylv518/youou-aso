CREATE TABLE wallet_transaction_type_config (
    transaction_type VARCHAR(64) PRIMARY KEY,
    display_name_zh VARCHAR(80) NOT NULL,
    display_name_en VARCHAR(120) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO wallet_transaction_type_config (transaction_type, display_name_zh, display_name_en)
VALUES
    ('ORDER_DEDUCT', '订单扣款', 'Order deduction'),
    ('ORDER_REFUND', '订单退款', 'Order refund'),
    ('ADMIN_RECHARGE', '管理员充值', 'Admin recharge'),
    ('ADMIN_ADJUSTMENT', '管理员调整', 'Admin adjustment');
