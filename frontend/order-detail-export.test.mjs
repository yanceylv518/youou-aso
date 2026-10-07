import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import test from 'node:test'
import ts from 'typescript'
import ExcelJS from 'exceljs'
import { build } from 'esbuild'
import { fileURLToPath } from 'node:url'

const moduleUrl = code => `data:text/javascript;base64,${Buffer.from(`${code}\n//# sourceURL=order-export-test-module.mjs`).toString('base64')}`
async function compile(path) {
  return ts.transpileModule(await readFile(new URL(path, import.meta.url), 'utf8'), {
    compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 }
  }).outputText
}
const settlementUrl = moduleUrl(await compile('./src/utils/orderSettlement.ts'))
const { orderSettlement } = await import(settlementUrl)
const timeUrl = moduleUrl(await compile('./src/utils/orderTime.ts'))
const exportCode = (await compile('./src/utils/orderDetailExport.ts'))
  .replace("'./orderSettlement'", JSON.stringify(settlementUrl))
  .replace("'./orderTime'", JSON.stringify(timeUrl))
  .replace("import('exceljs')", `import(${JSON.stringify(import.meta.resolve('exceljs'))})`)
const { createSheetConfig, exportOrderDetailExcel } = await import(moduleUrl(exportCode))
const labels = {
  'ordersPage.store': '应用商店', 'ordersPage.orderTime': '订单时间',
  'ordersPage.appIdentifier': 'App ID / Bundle ID', 'ordersPage.region': '国家/地区',
  'orderCreate.keywords': '关键词', 'orderCreate.chartType': '榜单类型',
  'orderCreate.targetRank': '目标排名', 'orderCreate.unitPrice': '单价（$）',
  'orderCreate.executionDays': '天数', 'ordersPage.amount': '金额'
}
const formatters = { storeLabel: () => 'App Store', regionLabel: code => code, label: key => labels[key] }
const order = {
  id: 1, orderNo: 'EXPORT-TEST', storeType: 'APP_STORE', regionCode: 'US',
  appIdentifier: '12345', orderStartDate: '2026-09-20', orderEndDate: '2026-09-22',
  items: [{ itemName: 'Negotiated summary, not a keyword', quantity: 1 }]
}
const items = [
  { regionCode: 'US', keyword: '测试关键词', targetRank: 5, unitPrice: 2.5, executionDays: 3, amount: 7.5 },
  { regionCode: 'JP', keyword: '第二个关键词', targetRank: 10, unitPrice: 4, executionDays: 2, amount: 8 }
]

for (const orderType of ['RANK_GUARANTEE', 'KEYWORD_COVERAGE']) {
  test(`${orderType} exports each actual keyword and region to XLSX`, async () => {
    let blob
    let filename
    const originalCreate = URL.createObjectURL
    const originalRevoke = URL.revokeObjectURL
    const originalDocument = globalThis.document
    URL.createObjectURL = value => { blob = value; return 'blob:export-test' }
    URL.revokeObjectURL = () => {}
    globalThis.document = { createElement: () => ({ click() { filename = this.download } }) }
    try {
      await exportOrderDetailExcel({ ...order, orderType }, formatters, items)
    } finally {
      URL.createObjectURL = originalCreate
      URL.revokeObjectURL = originalRevoke
      if (originalDocument === undefined) delete globalThis.document
      else globalThis.document = originalDocument
    }
    assert.match(filename, /\.xlsx$/)
    const workbook = new ExcelJS.Workbook()
    await workbook.xlsx.load(await blob.arrayBuffer())
    const sheet = workbook.worksheets[0]
    assert.equal(sheet.rowCount, 3)
    assert.equal(sheet.getCell('E1').value, '关键词')
    assert.equal(sheet.getCell('E2').value, '测试关键词')
    assert.equal(sheet.getCell('E3').value, '第二个关键词')
    assert.equal(sheet.getCell('D3').value, 'JP')
    assert.equal(sheet.getCell('F1').value, orderType === 'RANK_GUARANTEE' ? '目标排名' : '单价（$）')
    assert.equal(sheet.getRow(1).values.some(value => String(value).includes('orderCreate.')), false)
  })
}

test('missing special details cannot silently export the summary as a keyword', () => {
  assert.throws(() => createSheetConfig({ ...order, orderType: 'KEYWORD_COVERAGE' }, formatters), /details are required/)
})

test('chart ranking uses chart type and retains its target rank', () => {
  const sheet = createSheetConfig({ ...order, orderType: 'CHART_RANK_GUARANTEE' }, formatters,
    [{ ...items[0], chartType: '免费榜' }])
  assert.equal(sheet.headers[4], '榜单类型')
  assert.equal(sheet.rows[0][4], '免费榜')
  assert.equal(sheet.rows[0][5], 5)
})

test('ordinary keyword install export still reads order items', () => {
  const sheet = createSheetConfig({ ...order, orderType: 'KEYWORD_INSTALL', executionHours: 4,
    items: [{ itemName: '安装词', quantity: 12, regionCode: 'US' }] }, formatters)
  assert.equal(sheet.rows[0][4], '安装词')
  assert.equal(sheet.rows[0][5], 12)
})

for (const orderType of ['DOWNLOAD', 'RATING', 'REVIEW']) {
  test(`${orderType} exports edited totals exactly, ignoring stale daily metadata`, () => {
    const result = createSheetConfig({ ...order, orderType, items: [
      { regionCode: 'US', itemType: `${orderType}_5`, quantity: 5, metadataJson: JSON.stringify({ dailyDownloadCount: 100 }) },
      ...(orderType === 'DOWNLOAD' ? [] : [{ regionCode: 'US', itemType: `${orderType}_4`, quantity: 2 }])
    ] }, formatters)
    assert.equal(result.rows.reduce((sum, row) => sum + row[4], 0), 5)
    assert.deepEqual(result.rows.map(row => row[4]), [2, 2, 1])
    if (orderType !== 'DOWNLOAD') assert.equal(result.rows.reduce((sum, row) => sum + row[5], 0), 2)
  })
}
const csvBundle = await build({
  entryPoints: [fileURLToPath(new URL('./src/utils/orderExport.ts', import.meta.url))],
  alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  bundle: true, platform: 'node', format: 'esm', write: false
})
const { csvCell } = await import(moduleUrl(csvBundle.outputFiles[0].text))
test('CSV neutralizes formulas and control prefixes while preserving numeric data', () => {
  for (const value of ['=1+1', '+CMD()', '-1+1', '@SUM(A1)', '  =1', '\t=1', '\r=1', '\n@SUM(A1)']) {
    assert.equal(csvCell(value), `"'${value}"`)
  }
  assert.equal(csvCell('normal "text"'), '"normal ""text"""')
  assert.equal(csvCell(-12), '"-12"')
})

test('completed adjustment displays 8 and $0.08 while preserving original refund accounting', () => {
  const adjusted = { ...order, status: 'COMPLETED', orderType: 'KEYWORD_INSTALL', quantity: 11, totalAmount: 0.11, refundAmount: 0.03,
    items: [{ itemName: '11', quantity: 11, completedQuantity: 8, unitPrice: 0.01, amount: 0.11 }] }
  const display = orderSettlement(adjusted)
  assert.equal(display.quantity, 8)
  assert.equal(display.totalAmount, 0.08)
  assert.equal(display.items[0].quantity, 8)
  assert.equal(display.items[0].amount, 0.08)
  assert.equal(display.items[0].itemName, '11')
  assert.equal(adjusted.quantity, 11)
  assert.equal(adjusted.totalAmount, 0.11)
  assert.equal(adjusted.items[0].quantity, 11)
  assert.equal(createSheetConfig(adjusted, formatters).rows[0][5], 8)
})
test('completed quantity zero and full refund do not fall back to original values', () => {
  const display = orderSettlement({ ...order, status: 'COMPLETED', quantity: 11, totalAmount: 0.11, refundAmount: 0.11,
    items: [{ quantity: 11, completedQuantity: 0, unitPrice: 0.01, amount: 0.11 }] })
  assert.equal(display.quantity, 0)
  assert.equal(display.totalAmount, 0)
  assert.equal(display.items[0].amount, 0)
})
test('legacy completed orders and orders still in progress retain their original quantities', () => {
  const original = { ...order, quantity: 11, totalAmount: 0.11,
    items: [{ quantity: 11, completedQuantity: null, unitPrice: 0.01, amount: 0.11 }] }
  assert.equal(orderSettlement({ ...original, status: 'COMPLETED' }).quantity, 11)
  assert.equal(orderSettlement({ ...original, status: 'PAUSED', items: [{ ...original.items[0], completedQuantity: 8 }] }).quantity, 11)
})
test('completed daily exports total the delivered quantities across all items', () => {
  const adjusted = { ...order, status: 'COMPLETED', orderType: 'DOWNLOAD', quantity: 11, totalAmount: 0.11, refundAmount: 0.03,
    items: [{ quantity: 6, completedQuantity: 5, unitPrice: 0.01, amount: 0.06 }, { quantity: 5, completedQuantity: 3, unitPrice: 0.01, amount: 0.05 }] }
  assert.equal(orderSettlement(adjusted).quantity, 8)
  assert.equal(createSheetConfig(adjusted, formatters).rows.reduce((sum,row)=>sum+row[4],0),8)
})
