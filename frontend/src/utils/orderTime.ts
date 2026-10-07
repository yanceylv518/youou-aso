const businessMinuteFormatter = new Intl.DateTimeFormat('en-CA', {
  timeZone: 'Asia/Shanghai', year: 'numeric', month: '2-digit', day: '2-digit',
  hour: '2-digit', minute: '2-digit', hourCycle: 'h23'
})

export function businessMinute(now = new Date()): string {
  const parts = Object.fromEntries(businessMinuteFormatter.formatToParts(now).map(part => [part.type, part.value]))
  return `${parts.year}-${parts.month}-${parts.day}T${parts.hour}:${parts.minute}`
}

export function defaultKeywordOrderTime(): string {
  return businessMinute(new Date(Date.now() + 60_000))
}

export function keywordOrderTimeIsPast(value: string, now = new Date()): boolean {
  return Boolean(value) && value.replace(' ', 'T').slice(0, 16) < businessMinute(now)
}

type ScheduledOrder = { orderType?: string | null; scheduledStartAt?: string | null; orderStartDate?: string | null; orderEndDate?: string | null }

export function isReservedOrder(order: ScheduledOrder & { status?: string | null }, now = new Date()): boolean {
  if (!['KEYWORD_INSTALL', 'DOWNLOAD', 'RATING', 'REVIEW'].includes(order.orderType || '')) return false
  if (!['PENDING_PAYMENT', 'PENDING_CONFIRM', 'PENDING_EXECUTION'].includes(order.status || '')) return false
  const start = order.orderType === 'KEYWORD_INSTALL' && order.scheduledStartAt
    ? order.scheduledStartAt.replace(' ', 'T').slice(0, 16)
    : order.orderStartDate ? `${order.orderStartDate.slice(0, 10)}T00:00` : ''
  return Boolean(start) && start > businessMinute(now)
}

export function formatOrderStart(order: ScheduledOrder): string {
  if (order.orderType === 'KEYWORD_INSTALL' && order.scheduledStartAt) {
    return order.scheduledStartAt.replace('T', ' ').slice(0, 16)
  }
  return order.orderStartDate?.slice(0, 10) || ''
}

export function formatOrderSchedule(order: ScheduledOrder): string {
  if (order.orderType === 'KEYWORD_INSTALL') return formatOrderStart(order)
  const start = order.orderStartDate?.slice(0, 10) || ''
  const end = order.orderEndDate?.slice(0, 10) || ''
  return !end || start === end ? start || '-' : `${start} → ${end}`
}
