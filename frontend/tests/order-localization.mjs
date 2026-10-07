import assert from 'node:assert/strict'
import { build } from 'esbuild'
import { resolve } from 'node:path'
const result = await build({
  stdin: { contents: "export { i18n } from './src/i18n'; export { orderModuleLabel } from './src/utils/orderModuleLabel'; export { exportOrdersCsv } from './src/utils/orderExport'", resolveDir: process.cwd(), loader: 'ts' },
  alias: { '@': resolve('src') }, bundle: true, platform: 'node', format: 'esm', write: false,
})
const { i18n, orderModuleLabel, exportOrdersCsv } = await import(`data:text/javascript;base64,${Buffer.from(result.outputFiles[0].text).toString('base64')}`)
const names = { 'zh-CN':'关键词安装（高级）', 'en-US':'Premium keyword installs', 'ru-RU':'Установки по ключевым словам', 'pt-PT':'Instalações por palavra-chave', 'es-ES':'Instalaciones por palabra clave' }
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
    assert.equal(orderModuleLabel({ ...order, orderModuleNames:undefined }), locale === 'zh-CN' ? names['zh-CN'] : i18n.global.t('ordersPage.types.KEYWORD_INSTALL'))
    assert.equal(orderModuleLabel({ orderType:'DOWNLOAD' }), i18n.global.t('ordersPage.types.DOWNLOAD'))
    exportOrdersCsv([order], { typeLabel:()=>'', storeLabel:()=>'', statusLabel:()=>'' })
    const csv = await csvBlob.text()
    assert.ok(csv.includes(names[locale]))
    if (locale !== 'zh-CN') assert.ok(!/[\u3400-\u9fff]/u.test(csv), `${locale} export contains Chinese UI text`)
  }
  console.log('Passed: five-language labels, legacy fallback, live locale switching and CSV localization')
} finally {
  URL.createObjectURL = originalCreate
  URL.revokeObjectURL = originalRevoke
  delete globalThis.document
}
