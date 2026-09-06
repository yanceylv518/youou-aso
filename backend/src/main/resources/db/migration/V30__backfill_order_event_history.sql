INSERT INTO aso_order_event
(order_id, event_type, created_by_admin_id, created_at)
SELECT o.id, 'PAUSED', o.executed_by_admin_id, COALESCE(o.updated_at, CURRENT_TIMESTAMP)
FROM aso_order o
WHERE o.status = 'PAUSED'
  AND NOT EXISTS (
      SELECT 1 FROM aso_order_event e
      WHERE e.order_id = o.id AND e.event_type = 'PAUSED'
  );

INSERT INTO aso_order_event
(order_id, event_type, amount_before, amount_after, created_at)
SELECT
    o.id,
    'UPDATED',
    CASE
        WHEN wt.remark = CONCAT('ORDER_QUANTITY_INCREASE:', o.order_no)
            THEN o.total_amount - wt.amount
        ELSE o.total_amount + wt.amount
    END,
    o.total_amount,
    wt.created_at
FROM wallet_transaction wt
JOIN aso_order o ON wt.related_order_id = o.id
WHERE (
    wt.remark = CONCAT('ORDER_QUANTITY_INCREASE:', o.order_no)
    OR wt.remark = CONCAT('ORDER_QUANTITY_DECREASE:', o.order_no)
)
AND NOT EXISTS (
    SELECT 1 FROM aso_order_event e
    WHERE e.order_id = o.id
      AND e.event_type = 'UPDATED'
      AND e.created_at = wt.created_at
);