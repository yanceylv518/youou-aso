ALTER TABLE wallet_transaction_type_config
    ADD COLUMN display_name_ru VARCHAR(120) NOT NULL DEFAULT '' AFTER display_name_en,
    ADD COLUMN display_name_pt VARCHAR(120) NOT NULL DEFAULT '' AFTER display_name_ru,
    ADD COLUMN display_name_es VARCHAR(120) NOT NULL DEFAULT '' AFTER display_name_pt;

UPDATE wallet_transaction_type_config SET
 display_name_ru = CASE transaction_type WHEN 'ORDER_DEDUCT' THEN 'Списание по заказу' WHEN 'ORDER_REFUND' THEN 'Возврат по заказу' WHEN 'ADMIN_RECHARGE' THEN 'Пополнение' WHEN 'ADMIN_ADJUSTMENT' THEN 'Корректировка' WHEN 'ADMIN_REFUND' THEN 'Возврат администратора' WHEN 'ADMIN_GIFT' THEN 'Бонусное зачисление' WHEN 'ADMIN_DEDUCT' THEN 'Корректирующее списание' WHEN 'DELIVERY' THEN 'Выполнение заказа' ELSE display_name_en END,
 display_name_pt = CASE transaction_type WHEN 'ORDER_DEDUCT' THEN 'Débito da encomenda' WHEN 'ORDER_REFUND' THEN 'Reembolso da encomenda' WHEN 'ADMIN_RECHARGE' THEN 'Carregamento' WHEN 'ADMIN_ADJUSTMENT' THEN 'Ajuste' WHEN 'ADMIN_REFUND' THEN 'Reembolso administrativo' WHEN 'ADMIN_GIFT' THEN 'Crédito de oferta' WHEN 'ADMIN_DEDUCT' THEN 'Débito de ajuste' WHEN 'DELIVERY' THEN 'Execução da encomenda' ELSE display_name_en END,
 display_name_es = CASE transaction_type WHEN 'ORDER_DEDUCT' THEN 'Cargo del pedido' WHEN 'ORDER_REFUND' THEN 'Reembolso del pedido' WHEN 'ADMIN_RECHARGE' THEN 'Recarga' WHEN 'ADMIN_ADJUSTMENT' THEN 'Ajuste' WHEN 'ADMIN_REFUND' THEN 'Reembolso administrativo' WHEN 'ADMIN_GIFT' THEN 'Abono promocional' WHEN 'ADMIN_DEDUCT' THEN 'Cargo de ajuste' WHEN 'DELIVERY' THEN 'Ejecución del pedido' ELSE display_name_en END;
