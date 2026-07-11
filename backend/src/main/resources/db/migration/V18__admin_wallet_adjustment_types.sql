INSERT INTO wallet_transaction_type_config (transaction_type, display_name_zh, display_name_en)
VALUES
    ('ADMIN_REFUND', '管理员退款', 'Admin refund'),
    ('ADMIN_GIFT', '赠送余额', 'Gift credit'),
    ('ADMIN_DEDUCT', '余额扣减', 'Balance deduction')
ON DUPLICATE KEY UPDATE
    display_name_zh = VALUES(display_name_zh),
    display_name_en = VALUES(display_name_en),
    updated_at = CURRENT_TIMESTAMP;
