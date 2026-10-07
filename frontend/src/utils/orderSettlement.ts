import type { Order } from '@/api/orders'

// The API retains the original charge/quantity for refunds and audit history.
// Completed order details instead show the quantities delivered and the net charge.
export function orderSettlement(order: Order): Pick<Order, 'quantity' | 'totalAmount' | 'items'> {
  if (order.status !== 'COMPLETED') {
    return { quantity: order.quantity, totalAmount: order.totalAmount, items: order.items }
  }
  const refundedCents = Math.round(Number(order.refundAmount ?? 0) * 100)
  const items = order.items.map(item => {
    const quantity = item.completedQuantity ?? (refundedCents > 0 ? 0 : item.quantity)
    const amount = quantity === item.quantity || quantity == null || item.unitPrice == null
      ? item.amount
      : Math.round(Math.round(Number(item.unitPrice) * 10000) * quantity / 100) / 100
    return { ...item, quantity, amount }
  })
  return {
    quantity: items.length ? items.reduce((sum, item) => sum + (item.quantity ?? 0), 0) : order.quantity,
    totalAmount: Math.max(0, Math.round(Number(order.totalAmount) * 100) - refundedCents) / 100,
    items
  }
}
