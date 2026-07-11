import type { StoreType } from '@/api/applications'
import type { Order, OrderItem, OrderType } from '@/api/orders'

interface OrderDetailExportFormatters {
  storeLabel: (store?: StoreType | null) => string
  regionLabel: (code?: string | null) => string
}

type DetailRowValue = string | number | null | undefined

interface SheetConfig {
  name: string
  headers: string[]
  rows: DetailRowValue[][]
}

const DAY_MS = 86400000

export async function exportOrderDetailExcel(order: Order, formatters: OrderDetailExportFormatters) {
  const workbook = await createExcelWorkbook()
  const sheetConfig = createSheetConfig(order, formatters)
  const worksheet = workbook.addWorksheet(sheetConfig.name)
  worksheet.addRow(sheetConfig.headers)
  sheetConfig.rows.forEach((row) => worksheet.addRow(row.map((value) => value ?? '')))

  const headerRow = worksheet.getRow(1)
  headerRow.font = { bold: true, color: { argb: 'FFFFFFFF' } }
  headerRow.fill = {
    type: 'pattern',
    pattern: 'solid',
    fgColor: { argb: 'FF7F7F7F' }
  }
  headerRow.alignment = { horizontal: 'center', vertical: 'middle' }
  worksheet.eachRow((row) => {
    row.eachCell((cell) => {
      cell.border = {
        top: { style: 'thin', color: { argb: 'FFD9D9D9' } },
        left: { style: 'thin', color: { argb: 'FFD9D9D9' } },
        bottom: { style: 'thin', color: { argb: 'FFD9D9D9' } },
        right: { style: 'thin', color: { argb: 'FFD9D9D9' } }
      }
      cell.alignment = { horizontal: 'center', vertical: 'middle' }
    })
  })
  worksheet.columns.forEach((column) => {
    let width = 12
    column.eachCell?.({ includeEmpty: true }, (cell) => {
      width = Math.max(width, String(cell.value ?? '').length + 2)
    })
    column.width = Math.min(width, 32)
  })

  const buffer = await workbook.xlsx.writeBuffer()
  downloadBlobFile(
    `orderDetail-${safeFilenamePart(order.orderNo || String(order.id))}-${timestamp()}.xlsx`,
    new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
  )
}

function createSheetConfig(order: Order, formatters: OrderDetailExportFormatters): SheetConfig {
  if (order.orderType === 'KEYWORD_INSTALL') {
    return keywordSheet(order, formatters)
  }
  if (order.orderType === 'DOWNLOAD') {
    return downloadSheet(order, formatters)
  }
  if (order.orderType === 'RATING' || order.orderType === 'REVIEW') {
    return scoreSheet(order, formatters)
  }
  return keywordSheet(order, formatters)
}

function keywordSheet(order: Order, formatters: OrderDetailExportFormatters): SheetConfig {
  const rows = order.items.map((item) => [
    formatters.storeLabel(order.storeType),
    dateOnly(order.orderStartDate),
    order.appIdentifier,
    areaName(formatters, item.regionCode || order.regionCode),
    item.itemName || '',
    item.quantity ?? 0,
    executionHoursText(order.executionHours)
  ])
  return {
    name: 'Keyword details',
    headers: ['Store', 'Date', 'App.Id/Bundle.Id', 'Area', 'keyword', 'count', 'time'],
    rows
  }
}

function downloadSheet(order: Order, formatters: OrderDetailExportFormatters): SheetConfig {
  const rows = dateRange(order).flatMap((date) => order.items.map((item) => [
    formatters.storeLabel(order.storeType),
    date,
    order.appIdentifier,
    areaName(formatters, item.regionCode || order.regionCode),
    dailyDownloadCount(item, order)
  ]))
  return {
    name: 'Download details',
    headers: ['Store', 'Date', 'App.Id/Bundle.Id', 'Area', 'count'],
    rows
  }
}

function scoreSheet(order: Order, formatters: OrderDetailExportFormatters): SheetConfig {
  const grouped = groupScoreItems(order)
  const rows = dateRange(order).flatMap((date) => Object.values(grouped).map((group) => [
    formatters.storeLabel(order.storeType),
    date,
    order.appIdentifier,
    areaName(formatters, group.regionCode || order.regionCode),
    group.star5,
    group.star4
  ]))
  return {
    name: order.orderType === 'REVIEW' ? 'Comment details' : 'Score details',
    headers: ['Store', 'Date', 'App.Id/Bundle.Id', 'Area', '5 Stars', '4 Stars'],
    rows
  }
}

function groupScoreItems(order: Order) {
  const days = Math.max(dateRange(order).length, 1)
  return order.items.reduce<Record<string, { regionCode: string | null, star5: number, star4: number }>>((result, item) => {
    const key = item.regionCode || order.regionCode || ''
    if (!result[key]) {
      result[key] = { regionCode: item.regionCode || order.regionCode, star5: 0, star4: 0 }
    }
    if (item.itemType === 'RATING_5' || item.itemType === 'REVIEW_5') {
      result[key].star5 += dailyScoreCount(item, order.orderType, days)
    }
    if (item.itemType === 'RATING_4' || item.itemType === 'REVIEW_4') {
      result[key].star4 += dailyScoreCount(item, order.orderType, days)
    }
    return result
  }, {})
}

function areaName(formatters: OrderDetailExportFormatters, code?: string | null) {
  const label = formatters.regionLabel(code)
  return label.replace(/\s*\([^)]*\)\s*$/, '')
}

function dailyDownloadCount(item: OrderItem, order: Order) {
  const metadata = parseMetadata(item.metadataJson)
  if (typeof metadata.dailyDownloadCount === 'number') {
    return metadata.dailyDownloadCount
  }
  return splitTotalCount(item.quantity, Math.max(dateRange(order).length, 1))
}

function dailyScoreCount(item: OrderItem, orderType: OrderType, days: number) {
  if (orderType === 'RATING') {
    return splitTotalCount(item.quantity, days)
  }
  return splitTotalCount(item.quantity, days)
}

function splitTotalCount(total: number | null | undefined, days: number) {
  if (!total) return 0
  return Math.round(total / Math.max(days, 1))
}

function parseMetadata(value?: string | null) {
  if (!value) return {} as Record<string, unknown>
  try {
    const parsed = JSON.parse(value)
    return parsed && typeof parsed === 'object' ? parsed as Record<string, unknown> : {}
  } catch {
    return {}
  }
}

function dateRange(order: Order) {
  const start = parseDate(order.orderStartDate)
  const end = parseDate(order.orderEndDate)
  if (!start || !end || end < start) {
    return [dateOnly(order.orderStartDate)]
  }
  const result: string[] = []
  for (let time = start.getTime(); time <= end.getTime(); time += DAY_MS) {
    result.push(formatDate(new Date(time)))
  }
  return result
}

function parseDate(value?: string | null) {
  const text = dateOnly(value)
  if (!text) return null
  const date = new Date(`${text}T00:00:00`)
  return Number.isNaN(date.getTime()) ? null : date
}

function dateOnly(value?: string | null) {
  return value ? String(value).slice(0, 10) : ''
}

function formatDate(value: Date) {
  const pad = (part: number) => String(part).padStart(2, '0')
  return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())}`
}

function executionHoursText(value?: number | null) {
  if (value === null || value === undefined) return ''
  return `${value} ${value > 1 ? 'hours' : 'hour'}`
}

async function createExcelWorkbook() {
  const ExcelJS = await import('exceljs')
  return new ExcelJS.Workbook()
}

function downloadBlobFile(filename: string, blob: Blob) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

function timestamp() {
  const now = new Date()
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}-${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}`
}

function safeFilenamePart(value: string) {
  return value.replace(/[\\/:*?"<>|]/g, '-')
}
