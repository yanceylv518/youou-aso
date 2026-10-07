-- Each audit can create at most one order. NULL is still allowed for ordinary orders.
CREATE UNIQUE INDEX uk_aso_order_source_audit ON aso_order (source_audit_id);
