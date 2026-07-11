UPDATE wallet_transaction_type_config
SET display_name_zh = '充值',
    display_name_en = 'Recharge'
WHERE transaction_type = 'ADMIN_RECHARGE';

UPDATE wallet_transaction_type_config
SET display_name_zh = '划扣',
    display_name_en = 'Deduction'
WHERE transaction_type = 'ADMIN_ADJUSTMENT';
