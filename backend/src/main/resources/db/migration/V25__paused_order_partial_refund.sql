ALTER TABLE aso_order_item
    ADD COLUMN completed_quantity INT NULL AFTER quantity;

ALTER TABLE aso_order
    ADD COLUMN refund_amount DECIMAL(18, 2) NULL AFTER refund_transaction_id;