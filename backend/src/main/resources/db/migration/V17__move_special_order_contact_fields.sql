ALTER TABLE special_order_audit
    ADD COLUMN contact_type VARCHAR(32) NULL AFTER requested_content,
    ADD COLUMN contact_value VARCHAR(128) NULL AFTER contact_type;

ALTER TABLE aso_order
    DROP COLUMN contact_type,
    DROP COLUMN contact_value,
    DROP COLUMN remark;
