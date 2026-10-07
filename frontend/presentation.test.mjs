import test from 'node:test'
import assert from 'node:assert/strict'
import ts from 'typescript'
import { readFile } from 'node:fs/promises'
async function load(name) {
  const source = await readFile(new URL(`./src/utils/${name}.ts`, import.meta.url), 'utf8')
  const code = ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 } }).outputText
  return import(`data:text/javascript;base64,${Buffer.from(code).toString('base64')}`)
}
const { orderTimeline, formatCurrency, statusTone, localizedRegion } = await load('presentation')
const { formatOrderSchedule } = await load('orderTime')
test('timeline removes duplicate milestones and orders late adjustments after completion', () => {
  const rows = orderTimeline({
    createdAt:'2026-07-02T08:00:00',confirmedAt:'2026-07-02T09:00:00',completedAt:'2026-07-03T10:00:00',
    events:[
      {id:4,eventType:'COMPLETION_ADJUSTED',createdAt:'2026-10-05T10:00:00'},
      {id:1,eventType:'CREATED',createdAt:'2026-07-02T08:00:00'},
      {id:3,eventType:'COMPLETED',createdAt:'2026-07-03T10:00:00'},
      {id:2,eventType:'CONFIRMED',createdAt:'2026-07-02T09:00:00'}
    ]
  }, key=>key, event=>event.eventType)
  assert.deepEqual(rows.map(r=>r.key),['CREATED','CONFIRMED','COMPLETED','event-4'])
})
test('timeline retains repeated pause/resume history and event-only legacy milestones', () => {
  const rows = orderTimeline({events:[
    {id:3,eventType:'PAUSED',createdAt:'2026-07-02T12:00:00'},
    {id:1,eventType:'CREATED',createdAt:'2026-07-02T08:00:00'},
    {id:2,eventType:'RESUMED',createdAt:'2026-07-02T10:00:00'}
  ]}, key=>key, event=>event.eventType)
  assert.deepEqual(rows.map(r=>r.key),['event-1','event-2','event-3'])
})
test('currency preserves small unit prices while totals use two decimals',()=>{
  assert.equal(formatCurrency('673.5'),'$673.50')
  assert.equal(formatCurrency(0.0001,4),'$0.0001')
  assert.equal(formatCurrency(1200),'$1,200.00')
  assert.equal(formatCurrency(null),'-')
  assert.equal(formatCurrency('invalid'),'-')
})
test('same-day schedule is concise; keyword minutes and cross-day ranges are retained',()=>{
  assert.equal(formatOrderSchedule({orderType:'DOWNLOAD',orderStartDate:'2026-10-05',orderEndDate:'2026-10-05'}),'2026-10-05')
  assert.equal(formatOrderSchedule({orderStartDate:'2026-10-05',orderEndDate:'2026-10-06'}),'2026-10-05 → 2026-10-06')
  assert.equal(formatOrderSchedule({orderType:'KEYWORD_INSTALL',scheduledStartAt:'2026-10-05T13:30:00'}),'2026-10-05 13:30')
  assert.equal(formatOrderSchedule({}),'-')
})
test('status tones distinguish waiting from cancellation; region names follow locale',()=>{
  assert.equal(statusTone('APPROVED_WAIT_SUBMIT'),'warning')
  assert.equal(statusTone('CANCELLED'),'danger')
  assert.equal(statusTone('PENDING_EXECUTION'),'primary')
  assert.equal(statusTone('COMPLETED'),'success')
  assert.equal(localizedRegion('CN','zh-CN'),'中国 (CN)')
  assert.equal(localizedRegion('CN','en-US'),'China (CN)')
})
