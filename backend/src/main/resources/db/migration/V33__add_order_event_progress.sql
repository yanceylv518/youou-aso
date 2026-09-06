ALTER TABLE aso_order_event
    ADD COLUMN completed_before INT NULL AFTER quantity_after,
    ADD COLUMN completed_after INT NULL AFTER completed_before;

UPDATE aso_order_event e
JOIN aso_order o ON o.id = e.order_id
LEFT JOIN (
    SELECT order_id, SUM(COALESCE(completed_quantity, 0)) AS completed_total
    FROM aso_order_item
    GROUP BY order_id
) progress ON progress.order_id = o.id
SET e.quantity_after = COALESCE(e.quantity_after, o.quantity),
    e.completed_after = COALESCE(e.completed_after, progress.completed_total)
WHERE e.event_type = 'UPDATED';

UPDATE aso_order_event e
JOIN aso_order o ON o.id = e.order_id
JOIN (
    SELECT order_id, MIN(unit_price) AS unit_price
    FROM aso_order_item
    GROUP BY order_id
    HAVING MIN(unit_price) = MAX(unit_price) AND MIN(unit_price) > 0
) pricing ON pricing.order_id = o.id
JOIN (
    SELECT order_id
    FROM aso_order_event
    WHERE event_type = 'UPDATED'
    GROUP BY order_id
    HAVING COUNT(*) = 1
) single_edit ON single_edit.order_id = o.id
SET e.quantity_before = CASE
        WHEN e.amount_before > e.amount_after
            THEN o.quantity + ROUND((e.amount_before - e.amount_after) / pricing.unit_price)
        WHEN e.amount_before < e.amount_after
            THEN o.quantity - ROUND((e.amount_after - e.amount_before) / pricing.unit_price)
        ELSE o.quantity
    END,
    e.quantity_after = o.quantity
WHERE e.event_type = 'UPDATED'
  AND e.quantity_before IS NULL;