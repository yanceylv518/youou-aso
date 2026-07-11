UPDATE wallet_transaction_type_config
SET display_name_zh = '默认充值',
    display_name_en = 'Default recharge'
WHERE transaction_type = 'ADMIN_RECHARGE';

UPDATE wallet_transaction_type_config
SET display_name_zh = '默认划扣',
    display_name_en = 'Default deduction'
WHERE transaction_type = 'ADMIN_ADJUSTMENT';

INSERT INTO wallet_transaction_type_config (transaction_type, display_name_zh, display_name_en)
VALUES ('DELIVERY', '配送', 'Delivery')
ON DUPLICATE KEY UPDATE
    display_name_zh = VALUES(display_name_zh),
    display_name_en = VALUES(display_name_en),
    updated_at = CURRENT_TIMESTAMP;
