export function statusTone(status?: string | null): 'success' | 'warning' | 'danger' | 'info' | 'primary' {
  if (status === 'COMPLETED' || status === 'SUBMITTED') return 'success'
  if (status === 'CANCELLED' || status === 'REJECTED') return 'danger'
  if (status === 'EXECUTING' || status === 'PENDING_EXECUTION') return 'primary'
  if (['PENDING_PAYMENT', 'PENDING_CONFIRM', 'PENDING_REVIEW', 'APPROVED_WAIT_SUBMIT', 'PAUSED'].includes(status || '')) return 'warning'
  return 'info'
}

export function formatCurrency(value: string | number | undefined | null, precision = 2): string {
  if (value === null || value === undefined || value === '' || !Number.isFinite(Number(value))) return '-'
  return '$' + Number(value).toLocaleString('en-US', { minimumFractionDigits: precision, maximumFractionDigits: precision })
}

export function localizedRegion(code: string, locale: string): string {
  try {
    const name = new Intl.DisplayNames([locale], { type: 'region' }).of(code.toUpperCase())
    return name && name !== code ? `${name} (${code})` : code
  } catch { return code }
}

type TimelineEvent = { id: number; eventType: string; createdAt: string }
type TimelineOrder<E> = { createdAt?: string | null; confirmedAt?: string | null; executedAt?: string | null; completedAt?: string | null; events?: E[] }

export function orderTimeline<E extends TimelineEvent>(order: TimelineOrder<E>, label: (key: string) => string, eventLabel: (event: E) => string) {
  const milestones = [
    ['CREATED', 'ordersPage.createdAt', order.createdAt],
    ['CONFIRMED', 'orderDetail.confirmedAt', order.confirmedAt],
    ['EXECUTED', 'orderDetail.executedAt', order.executedAt],
    ['COMPLETED', 'orderDetail.completedAt', order.completedAt]
  ] as const
  const present = new Set(milestones.filter(([, , date]) => date).map(([type]) => type as string))
  const entries: { key: string; label: string; value: string; sort: number }[] = milestones.filter(([, , date]) => date).map(([key, name, date]) => ({ key, label: label(name), value: date!.replace('T', ' '), sort: 0 }))
  for (const event of order.events || []) {
    if (present.has(event.eventType)) continue
    entries.push({ key: `event-${event.id}`, label: eventLabel(event), value: event.createdAt.replace('T', ' '), sort: event.id })
  }
  return entries.sort((a, b) => a.value.localeCompare(b.value) || a.sort - b.sort)
}
