UPDATE aso_order_event paused_event
JOIN aso_order_event updated_event
  ON updated_event.order_id = paused_event.order_id
 AND updated_event.event_type = 'UPDATED'
 AND TIMESTAMPDIFF(SECOND, paused_event.created_at, updated_event.created_at) = 1
SET paused_event.created_at = DATE_ADD(paused_event.created_at, INTERVAL 8 HOUR)
WHERE paused_event.event_type = 'PAUSED';

UPDATE aso_order_event event_record
JOIN wallet_transaction wt
  ON event_record.order_id = wt.related_order_id
 AND event_record.created_at = wt.created_at
 AND (
      wt.remark LIKE 'ORDER_QUANTITY_INCREASE:%'
      OR wt.remark LIKE 'ORDER_QUANTITY_DECREASE:%'
 )
SET event_record.created_at = DATE_ADD(event_record.created_at, INTERVAL 8 HOUR)
WHERE event_record.event_type = 'UPDATED';

UPDATE wallet_transaction
SET created_at = DATE_ADD(created_at, INTERVAL 8 HOUR);