import { readFileSync } from 'node:fs'
import assert from 'node:assert/strict'
import { build } from 'esbuild'
import { resolve } from 'node:path'
const result = await build({
  stdin: { contents: "export { i18n } from './src/i18n'; export { localizedModule } from './src/utils/moduleLocalization'; export { orderModuleLabel } from './src/utils/orderModuleLabel'; export { exportOrdersCsv } from './src/utils/orderExport'", resolveDir: process.cwd(), loader: 'ts' },
  alias: { '@': resolve('src') }, bundle: true, platform: 'node', format: 'esm', write: false,
})
const { i18n, localizedModule, orderModuleLabel, exportOrdersCsv } = await import(`data:text/javascript;base64,${Buffer.from(result.outputFiles[0].text).toString('base64')}`)
const names = { 'zh-CN':'关键词安装（高级）', 'en-US':'Premium keyword installs', 'ru-RU':'Премиальный пакет', 'pt-PT':'Pacote premium', 'es-ES':'Paquete prémium' }
let csvBlob
const originalCreate = URL.createObjectURL
const originalRevoke = URL.revokeObjectURL
URL.createObjectURL = blob => { csvBlob = blob; return 'blob:test' }
URL.revokeObjectURL = () => {}
globalThis.document = { createElement: () => ({ click() {} }) }
try {
  for (const locale of Object.keys(names)) {
    i18n.global.locale.value = locale
    const order = { orderType:'KEYWORD_INSTALL', orderModuleName:names['zh-CN'], orderModuleNames:names }
    assert.equal(orderModuleLabel(order), names[locale])
    assert.equal(orderModuleLabel({ ...order, orderModuleNames:undefined }), locale === 'zh-CN' ? names['zh-CN'] : i18n.global.t('ordersPage.types.KEYWORD_INSTALL') + ' (' + ({ 'en-US':'Advanced', 'ru-RU':'Расширенный', 'pt-PT':'Avançado', 'es-ES':'Avanzado' })[locale] + ')')
    assert.equal(orderModuleLabel({ orderType:'DOWNLOAD' }), i18n.global.t('ordersPage.types.DOWNLOAD'))
    exportOrdersCsv([order], { typeLabel:()=>'', storeLabel:()=>'', statusLabel:()=>'' })
    const csv = await csvBlob.text()
    assert.ok(csv.includes(names[locale]))
    if (locale !== 'zh-CN') assert.ok(!/[\u3400-\u9fff]/u.test(csv), `${locale} export contains Chinese UI text`)
  }
  const glossary = JSON.parse(readFileSync('../backend/src/main/resources/i18n/service-names.json', 'utf8'))
  const types = ['KEYWORD_INSTALL', 'DOWNLOAD', 'RATING', 'REVIEW', 'RANK_GUARANTEE', 'CHART_RANK_GUARANTEE', 'KEYWORD_COVERAGE', 'KEYWORD_INSTALL', 'KEYWORD_INSTALL']
  for (const [index, entry] of glossary.entries()) {
    for (const [locale, expected] of Object.entries(entry)) {
      i18n.global.locale.value = locale
      assert.equal(orderModuleLabel({ orderType: types[index], orderModuleName: entry['zh-CN'] }), expected, `${types[index]} / ${locale}`)
    }
  }
  i18n.global.locale.value = 'en-US'
  for (const [source, legacy, expected] of [
    ['关键词安装（普通）', 'Keyword installs(General)', 'Keyword installs (Standard)'],
    ['关键词安装(高级）', 'Key installation (advanced)', 'Keyword installs (Advanced)']
  ]) {
    const module = { moduleName: source, moduleNameEn: legacy, orderType: 'KEYWORD_INSTALL' }
    assert.equal(localizedModule(module, 'moduleName'), expected)
    assert.equal(orderModuleLabel({ orderModuleName: source, orderModuleNames: { 'en-US': legacy }, orderType: module.orderType }), expected)
  }
  assert.equal(localizedModule({ moduleName: '自定义服务', moduleNameEn: 'Custom service' }, 'moduleName', 'es-ES'), 'Custom service')
  assert.equal(orderModuleLabel({ orderModuleName: '自定义服务', orderModuleNames: { 'en-US': 'Custom service' } }), 'Custom service')
  console.log('Passed: five-language labels, legacy fallback, live locale switching and CSV localization')
} finally {
  URL.createObjectURL = originalCreate
  URL.revokeObjectURL = originalRevoke
  delete globalThis.document
}
