-- An early execution must not start a future keyword order's timer before its
-- scheduled start date. Preserve any deadline that is already later.
UPDATE aso_order
SET expected_completed_at = DATE_ADD(
        GREATEST(executed_at, COALESCE(CAST(order_start_date AS DATETIME), executed_at)),
        INTERVAL GREATEST(COALESCE(execution_hours, 1), 1) HOUR
    ),
    updated_at = updated_at
WHERE order_type = 'KEYWORD_INSTALL'
  AND status IN ('PENDING_CONFIRM', 'PENDING_EXECUTION', 'EXECUTING', 'PAUSED')
  AND executed_at IS NOT NULL
  AND expected_completed_at < DATE_ADD(
        GREATEST(executed_at, COALESCE(CAST(order_start_date AS DATETIME), executed_at)),
        INTERVAL GREATEST(COALESCE(execution_hours, 1), 1) HOUR
    );

-- Older special orders always persisted a single day even when the priced
-- audit items contained a longer duration. Only repair that legacy shape,
-- using valid persisted item durations; leave historical terminal orders alone.
UPDATE aso_order o
JOIN special_order_audit a
  ON a.id = o.source_audit_id
 AND a.order_type = o.order_type
JOIN (
    SELECT audit_id, MAX(execution_days) AS execution_days
    FROM special_order_audit_item
    WHERE execution_days BETWEEN 1 AND 3650
    GROUP BY audit_id
    HAVING MAX(execution_days) > 1
) duration ON duration.audit_id = a.id
SET o.total_days = duration.execution_days,
    o.order_end_date = DATE_ADD(o.order_start_date, INTERVAL (duration.execution_days - 1) DAY),
    o.expected_completed_at = GREATEST(
        COALESCE(o.expected_completed_at, CAST(o.order_start_date AS DATETIME)),
        DATE_ADD(CAST(o.order_start_date AS DATETIME), INTERVAL duration.execution_days DAY)
    ),
    o.updated_at = o.updated_at
WHERE o.order_type IN ('RANK_GUARANTEE', 'CHART_RANK_GUARANTEE', 'KEYWORD_COVERAGE')
  AND o.status IN ('PENDING_CONFIRM', 'PENDING_EXECUTION', 'EXECUTING', 'PAUSED')
  AND o.order_start_date IS NOT NULL
  AND o.total_days = 1
  AND o.order_end_date = o.order_start_date;
