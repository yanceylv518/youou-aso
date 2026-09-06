UPDATE wallet_transaction wt
JOIN aso_order o
  ON wt.id = o.deducted_transaction_id
  OR wt.id = o.refund_transaction_id
SET wt.related_order_id = o.id
WHERE wt.related_order_id IS NULL;

UPDATE wallet_transaction wt
JOIN aso_order o
  ON wt.remark = CONCAT('ORDER_REFUND:', o.order_no)
  OR wt.remark = CONCAT('ORDER_QUANTITY_INCREASE:', o.order_no)
  OR wt.remark = CONCAT('ORDER_QUANTITY_DECREASE:', o.order_no)
  OR wt.remark = CONCAT('ORDER_DEDUCT_RETRY:', o.order_no)
SET wt.related_order_id = o.id
WHERE wt.related_order_id IS NULL;