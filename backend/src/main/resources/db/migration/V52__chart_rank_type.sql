ALTER TABLE special_order_audit_item
    ADD COLUMN chart_type VARCHAR(255) NULL AFTER keyword;

UPDATE special_order_audit_item item
JOIN special_order_audit audit ON audit.id = item.audit_id
SET item.chart_type = item.keyword
WHERE audit.order_type = 'CHART_RANK_GUARANTEE'
  AND item.chart_type IS NULL;
