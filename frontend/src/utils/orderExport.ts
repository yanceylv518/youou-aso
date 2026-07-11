import type { StoreType } from '@/api/applications'
import type { OrderListStatus, OrderType } from '@/api/orders'

export interface ExportOrderRow {
  customerUsername?: string | null
  customerEmail?: string | null
  orderType?: OrderType | string | null
  storeType?: StoreType | string | null
  appIdentifier?: string | null
  status?: OrderListStatus | string | null
  orderStartDate?: string | null
  orderEndDate?: string | null
  totalDays?: number | null
  totalAmount?: number | string | null
}

export interface ExportOrderLabelFormatters<T extends ExportOrderRow> {
  typeLabel: (type?: T['orderType']) => string
  storeLabel: (store?: T['storeType']) => string
  statusLabel: (status?: T['status']) => string
}

const ORDER_EXPORT_HEADERS = [
  '账号',
  '订单类型',
  '商店类型',
  '应用ID',
  '订单开始日期',
  '订单结束日期',
  '订单总天数',
  '订单状态',
  '订单金额',
  '联系方式',
  '联系号码'
]

export function exportOrdersCsv<T extends ExportOrderRow>(rows: T[], formatters: ExportOrderLabelFormatters<T>, filenamePrefix = 'order') {
  const lines = rows.map((row) => [
    row.customerEmail || row.customerUsername || '',
    formatters.typeLabel(row.orderType),
    formatters.storeLabel(row.storeType),
    row.appIdentifier || '',
    dateOnly(row.orderStartDate),
    dateOnly(row.orderEndDate),
    orderTotalDays(row),
    formatters.statusLabel(row.status),
    row.totalAmount ?? '',
    '',
    ''
  ])
  const csv = [ORDER_EXPORT_HEADERS, ...lines].map((line) => line.map(csvCell).join(',')).join('\r\n')
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${filenamePrefix}-${timestamp()}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

function dateOnly(value?: string | null) {
  return value ? String(value).slice(0, 10) : ''
}

function orderTotalDays(row: ExportOrderRow) {
  if (row.totalDays !== null && row.totalDays !== undefined) {
    return row.totalDays
  }
  const startDate = dateOnly(row.orderStartDate)
  const endDate = dateOnly(row.orderEndDate)
  if (!startDate || !endDate) {
    return ''
  }
  const start = new Date(`${startDate}T00:00:00`)
  const end = new Date(`${endDate}T00:00:00`)
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime()) || end < start) {
    return ''
  }
  return Math.floor((end.getTime() - start.getTime()) / 86400000) + 1
}

function timestamp() {
  const now = new Date()
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}-${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}`
}

function csvCell(value: string | number | null | undefined) {
  return `"${String(value ?? '').replace(/"/g, '""')}"`
}
