import { i18n, type AppLocale } from '@/i18n'
import { orderModuleLabel } from './orderModuleLabel'
import { formatOrderStart } from './orderTime'
import type { StoreType } from '@/api/applications'
import type { OrderListStatus, OrderType } from '@/api/orders'

export interface ExportOrderRow {
  customerUsername?: string | null
  customerEmail?: string | null
  orderType?: OrderType | string | null
  orderModuleName?: string | null
  orderModuleNames?: Partial<Record<string, string>>
  storeType?: StoreType | string | null
  appIdentifier?: string | null
  status?: OrderListStatus | string | null
  scheduledStartAt?: string | null
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

const ORDER_EXPORT_HEADERS: Record<AppLocale, string[]> = {
  'zh-CN': ['账号', '订单类型', '商店类型', '应用ID', '订单开始时间', '订单结束日期', '订单总天数', '订单状态', '订单金额', '联系方式', '联系号码'],
  'en-US': ['Account', 'Task type', 'Store', 'App ID', 'Order start time', 'Order end date', 'Total days', 'Status', 'Amount', 'Contact method', 'Contact number'],
  'ru-RU': ['Аккаунт', 'Тип задачи', 'Магазин', 'ID приложения', 'Начало заказа', 'Дата окончания', 'Всего дней', 'Статус', 'Сумма', 'Способ связи', 'Контактный номер'],
  'pt-PT': ['Conta', 'Tipo de tarefa', 'Loja', 'ID da aplicação', 'Início do pedido', 'Data de fim', 'Total de dias', 'Estado', 'Montante', 'Método de contacto', 'Número de contacto'],
  'es-ES': ['Cuenta', 'Tipo de tarea', 'Tienda', 'ID de aplicación', 'Inicio del pedido', 'Fecha de finalización', 'Días totales', 'Estado', 'Importe', 'Método de contacto', 'Número de contacto']
}

export function exportOrdersCsv<T extends ExportOrderRow>(rows: T[], formatters: ExportOrderLabelFormatters<T>, filenamePrefix = 'order') {
  const lines = rows.map((row) => [
    row.customerEmail || row.customerUsername || '',
    orderModuleLabel(row),
    formatters.storeLabel(row.storeType),
    row.appIdentifier || '',
    formatOrderStart(row),
    dateOnly(row.orderEndDate),
    orderTotalDays(row),
    formatters.statusLabel(row.status),
    row.totalAmount ?? '',
    '',
    ''
  ])
  const csv = [ORDER_EXPORT_HEADERS[i18n.global.locale.value], ...lines].map((line) => line.map(csvCell).join(',')).join('\r\n')
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

export function csvCell(value: string | number | null | undefined) {
  const text = String(value ?? '')
  const safe = typeof value === 'string' && /^[\s\u0000-\u001f]*[=+@-]|^[\t\r\n]/.test(text) ? `'${text}` : text
  return `"${safe.replace(/"/g, '""')}"`
}
