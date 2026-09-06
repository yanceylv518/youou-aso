UPDATE aso_order_event paused_event
JOIN (
    SELECT order_id, MIN(created_at) AS first_updated_at
    FROM aso_order_event
    WHERE event_type = 'UPDATED'
    GROUP BY order_id
) updated_event ON updated_event.order_id = paused_event.order_id
SET paused_event.created_at = DATE_SUB(updated_event.first_updated_at, INTERVAL 1 SECOND)
WHERE paused_event.event_type = 'PAUSED'
  AND paused_event.created_at >= updated_event.first_updated_at;