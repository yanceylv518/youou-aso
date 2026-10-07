ALTER TABLE aso_order ADD COLUMN scheduled_start_at DATETIME NULL AFTER order_start_date;

-- Existing date-only keyword orders retain their original midnight schedule.
UPDATE aso_order
SET scheduled_start_at = CAST(order_start_date AS DATETIME), updated_at = updated_at
WHERE order_type = 'KEYWORD_INSTALL' AND scheduled_start_at IS NULL;
