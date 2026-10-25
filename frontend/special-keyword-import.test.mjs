import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import vm from 'node:vm'
import ts from 'typescript'
const parserSource = readFileSync(new URL('./src/utils/coverageKeywords.ts', import.meta.url), 'utf8')
const parserCode = ts.transpileModule(parserSource, { compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.ESNext } }).outputText
const parser = await import(`data:text/javascript;base64,${Buffer.from(parserCode).toString('base64')}`)
for (const role of ['admin', 'user']) {
  const source = readFileSync(new URL(`./src/views/${role}/OrderCreateView.vue`, import.meta.url), 'utf8')
  const functions = ['createSpecialItem', 'confirmBatchKeywordImport'].map(name => source.match(new RegExp(`function ${name}\\([^)]*\\)[^{]*\\{[\\s\\S]*?\\n\\}`))[0]).join('\n')
  const code = ts.transpileModule(functions, { compilerOptions: { target: ts.ScriptTarget.ES2022 } }).outputText
  function setup(type, input) {
    const existing = { keyword: 'existing', targetRank: 5, unitPrice: 2, executionDays: 3 }
    const group = { regionCode: 'US', items: [existing, { keyword: '', targetRank: 1 }] }
    const untouched = { regionCode: 'JP', items: [{ keyword: 'other' }] }
    const context = { ...parser, form: { orderType: type, specialGroups: [untouched, group] }, batchKeywordMode: { value: 'special' }, batchKeywordGroupIndex: { value: 1 }, batchKeywordText: { value: input }, batchKeywordDialogVisible: { value: true }, ElMessage: { warning() {}, success() {} }, t: key => key }
    const run = vm.runInNewContext(code + '; confirmBatchKeywordImport', context)
    return { run, group, untouched, existing, context }
  }
  for (const type of ['RANK_GUARANTEE', 'KEYWORD_COVERAGE']) {
    test(`${role} ${type}: import into selected region, deduplicate and preserve existing settings`, () => {
      const fixture = setup(type, ' existing \r\n new keyword\n\nnew keyword\r第二个关键词')
      fixture.run()
      assert.deepEqual(Array.from(fixture.group.items, item => item.keyword), ['existing', 'new keyword', '第二个关键词'])
      assert.equal(fixture.group.items[0], fixture.existing)
      assert.equal(fixture.group.items[1].targetRank, 1)
      if (role === 'admin') {
        assert.equal(fixture.group.items[1].executionDays, 1)
        assert.equal(fixture.group.items[1].unitPrice, null)
      }
      assert.deepEqual(fixture.untouched.items, [{ keyword: 'other' }])
      assert.equal(fixture.context.batchKeywordDialogVisible.value, false)
    })
  }
  test(`${role}: empty or invalid input never partially imports; chart ranking is excluded`, () => {
    for (const [type, input] of [['RANK_GUARANTEE', '\n  '], ['RANK_GUARANTEE', 'valid\n' + 'x'.repeat(256)], ['CHART_RANK_GUARANTEE', 'keyword']]) {
      const fixture = setup(type, input)
      const original = fixture.group.items
      fixture.run()
      assert.equal(fixture.group.items, original)
      assert.equal(fixture.context.batchKeywordDialogVisible.value, true)
    }
  })
}
