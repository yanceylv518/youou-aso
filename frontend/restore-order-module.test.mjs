import test from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import ts from 'typescript'
const source = await readFile(new URL('./src/utils/restoreOrderModule.ts', import.meta.url), 'utf8')
const code = ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 } }).outputText
const { restoreOrderModuleId } = await import(`data:text/javascript;base64,${Buffer.from(code).toString('base64')}`)
const modules = [
  { id: 1, orderType: 'KEYWORD_INSTALL', moduleName: '普通', enabled: true },
  { id: 2, orderType: 'KEYWORD_INSTALL', moduleName: '高级', enabled: true },
  { id: 3, orderType: 'KEYWORD_COVERAGE', moduleName: '覆盖', enabled: true }
]
test('renewal restores exact module rather than the first module of the same task type', () => {
  assert.equal(restoreOrderModuleId({ orderType: 'KEYWORD_INSTALL', orderModuleId: 2 }, modules), 2)
  assert.equal(restoreOrderModuleId({ orderType: 'KEYWORD_COVERAGE', orderModuleId: 3 }, modules), 3)
})
test('legacy orders resolve by saved name or an unambiguous task type', () => {
  assert.equal(restoreOrderModuleId({ orderType: 'KEYWORD_INSTALL', orderModuleName: '高级' }, modules), 2)
  assert.equal(restoreOrderModuleId({ orderType: 'KEYWORD_COVERAGE' }, modules), 3)
  assert.equal(restoreOrderModuleId({ orderType: 'KEYWORD_INSTALL' }, modules), null)
})
test('unavailable or mismatched original modules are never silently replaced', () => {
  assert.equal(restoreOrderModuleId({ orderType: 'KEYWORD_INSTALL', orderModuleId: 3 }, modules), null)
  assert.equal(restoreOrderModuleId({ orderType: 'KEYWORD_INSTALL', orderModuleId: 9 }, modules), null)
  assert.equal(restoreOrderModuleId({ orderType: 'KEYWORD_INSTALL', orderModuleId: 2 }, modules.map(m => ({ ...m, enabled: m.id !== 2 }))), null)
})
