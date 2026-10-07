import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import vm from 'node:vm'
import ts from 'typescript'
import { computed, reactive, ref, watch } from 'vue'

function loadFunction(file, name, context) {
  const text = readFileSync(new URL(file, import.meta.url), 'utf8')
  const source = text.match(new RegExp(`function ${name}\\(\\) \\{[\\s\\S]*?\\n\\}`))[0]
  const js = ts.transpileModule(source, { compilerOptions: { target: ts.ScriptTarget.ES2022 } }).outputText
  return vm.runInNewContext(js + `; ${name}`, context)
}
for (const role of ['admin', 'user']) {
  test(`${role}: all three list entries carry their own store`, () => {
    for (const store of ['APP_STORE', 'GOOGLE_PLAY', 'IPAD_STORE']) {
      let destination
      const create = loadFunction(`./src/views/${role}/StoreOrdersView.vue`, 'createOrder', {
        router: { push: route => { destination = route } }, storeType: ref(store), filters: { storeType: '' }
      })
      create()
      assert.equal(destination.name, `${role}-order-create`)
      assert.equal(destination.query.storeType, store)
    }
  })
  test(`${role}: module defaults preserve the requested store, including store-only modules`, () => {
    const modules = [
      { id: 1, enabled: true, orderType: 'KEYWORD_INSTALL', storeTypes: ['APP_STORE'] },
      { id: 2, enabled: true, orderType: 'KEYWORD_INSTALL', storeTypes: ['GOOGLE_PLAY'] },
      { id: 3, enabled: true, orderType: 'DOWNLOAD', storeTypes: ['IPAD_STORE'] }
    ]
    for (const [index, store] of ['APP_STORE', 'GOOGLE_PLAY', 'IPAD_STORE'].entries()) {
      const form = reactive({ orderType: 'KEYWORD_INSTALL', storeType: store })
      const id = ref(null)
      const selected = computed(() => modules.find(m => m.id === id.value && m.orderType === form.orderType))
      const stop = watch(selected, module => {
        if (module && !module.storeTypes.includes(form.storeType)) form.storeType = module.storeTypes[0]
      }, { flush: 'sync' })
      const available = ref(modules)
      const select = loadFunction(`./src/views/${role}/OrderCreateView.vue`, 'ensureSelectedOrderModule', {
        route: { query: { storeType: store } }, form, selectedOrderModule: selected,
        selectedOrderModuleId: id, availableOrderModules: available
      })
      select()
      assert.equal(id.value, index + 1)
      assert.equal(form.storeType, store)
      id.value = null
      available.value = modules.filter(m => !m.storeTypes.includes(store))
      select()
      assert.equal(id.value, null, 'No compatible module must not silently switch stores')
      assert.equal(form.storeType, store)
      stop()
    }
  })
}
