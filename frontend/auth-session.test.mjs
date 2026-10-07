import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { createRequire } from 'node:module'
import vm from 'node:vm'
import path from 'node:path'
import ts from 'typescript'
import { createPinia } from 'pinia'

const require = createRequire(import.meta.url)
const storage = () => {
  const values = new Map()
  return { getItem: k => values.get(k) ?? null, setItem: (k, v) => values.set(k, v), removeItem: k => values.delete(k) }
}
function tab(shared, session = storage()) {
  const window = { localStorage: shared, sessionStorage: session, location: { pathname: '/admin/dashboard', search: '', href: '' } }
  const cache = new Map()
  function load(name) {
    if (cache.has(name)) return cache.get(name)
    if (!name.startsWith('@/')) return require(name)
    const filename = new URL(`./src/${name.slice(2)}.ts`, import.meta.url)
    const source = readFileSync(filename, 'utf8')
    const code = ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022, esModuleInterop: true } }).outputText
    const module = { exports: {} }
    cache.set(name, module.exports)
    const localRequire = dependency => load(dependency.startsWith('.') ? path.posix.join(path.posix.dirname(name), dependency) : dependency)
    vm.runInNewContext(`(function(require, module, exports) {${code}\n})`, { window })(localRequire, module, module.exports)
    return module.exports
  }
  const auth = load('@/stores/auth').useAuthStore(createPinia())
  const http = load('@/api/http').http
  http.defaults.adapter = async config => ({ data: {}, status: 200, statusText: 'OK', headers: {}, config })
  return { auth, http, window, session }
}
const login = (tab, token, accountType = 'ADMIN') => tab.auth.applyLoginResult({ token, accountId: 1, username: token, email: '', accountType, roleCode: accountType === 'ADMIN' ? 'ADMIN' : '', forcePasswordChange: false })

test('two tabs keep their own identity and request token; logout is isolated', async () => {
  const shared = storage(), admin = tab(shared), customer = tab(shared)
  login(admin, 'admin-token'); login(customer, 'customer-token', 'CUSTOMER')
  assert.equal((await admin.http.get('/test')).config.headers.Authorization, 'Bearer admin-token')
  assert.equal((await customer.http.get('/test')).config.headers.Authorization, 'Bearer customer-token')
  const refreshedAdmin = tab(shared, admin.session)
  refreshedAdmin.auth.restore()
  assert.equal(refreshedAdmin.auth.token, 'admin-token')
  assert.equal(refreshedAdmin.auth.accountType, 'ADMIN')
  const refreshed = tab(shared, customer.session)
  refreshed.auth.restore()
  assert.equal(refreshed.auth.accountType, 'CUSTOMER')
  admin.auth.logout()
  assert.equal((await admin.http.get('/test')).config.headers.Authorization, undefined)
  assert.equal((await customer.http.get('/test')).config.headers.Authorization, 'Bearer customer-token')
  const freshTab = tab(shared)
  freshTab.auth.restore()
  assert.equal(freshTab.auth.token, '')
})

test('legacy shared login is ignored; copied tabs can switch accounts independently', async () => {
  const shared = storage()
  shared.setItem('youou_aso_auth', JSON.stringify({ token: 'legacy-admin' }))
  const original = tab(shared)
  original.auth.restore()
  assert.equal(original.auth.isLoggedIn, false)
  assert.equal(shared.getItem('youou_aso_auth'), null)
  login(original, 'first')
  const copyStorage = storage()
  copyStorage.setItem('youou_aso_tab_auth', original.session.getItem('youou_aso_tab_auth'))
  const copy = tab(shared, copyStorage)
  copy.auth.restore()
  assert.equal(copy.auth.token, 'first')
  copy.auth.logout(); login(copy, 'second', 'CUSTOMER')
  assert.equal((await original.http.get('/test')).config.headers.Authorization, 'Bearer first')
})

test('expired response logs out only its tab; stale response cannot clear a newer login', async () => {
  const shared = storage(), first = tab(shared), second = tab(shared)
  login(first, 'old'); login(second, 'other')
  let rejectPending
  first.http.defaults.adapter = config => new Promise((_, reject) => { rejectPending = () => reject({ config, response: { status: 401 } }) })
  const pending = first.http.get('/test')
  await new Promise(resolve => setImmediate(resolve))
  login(first, 'new')
  rejectPending()
  await assert.rejects(pending)
  assert.equal(first.window.location.href, '')
  first.auth.restore()
  assert.equal(first.auth.token, 'new')
  first.http.defaults.adapter = async config => { throw { config, response: { status: 401 } } }
  await assert.rejects(first.http.get('/test'))
  assert.match(first.window.location.href, /^\/login\?redirect=/)
  assert.equal(first.session.getItem('youou_aso_tab_auth'), null)
  assert.equal((await second.http.get('/test')).config.headers.Authorization, 'Bearer other')
})

test('blocked tab storage uses page memory without shared-storage fallback', async () => {
  const blocked = { getItem() { throw Error('blocked') }, setItem() { throw Error('blocked') }, removeItem() { throw Error('blocked') } }
  const shared = storage(), first = tab(shared, blocked), second = tab(shared, blocked)
  login(first, 'first'); login(second, 'second')
  assert.equal((await first.http.get('/test')).config.headers.Authorization, 'Bearer first')
  first.auth.logout()
  assert.equal((await first.http.get('/test')).config.headers.Authorization, undefined)
  assert.equal((await second.http.get('/test')).config.headers.Authorization, 'Bearer second')
  assert.equal(shared.getItem('youou_aso_tab_auth'), null)
})

test('malformed session is discarded and business 403 does not log out', async () => {
  const current = tab(storage())
  current.session.setItem('youou_aso_tab_auth', '{broken')
  assert.equal((await current.http.get('/test')).config.headers.Authorization, undefined)
  assert.equal(current.session.getItem('youou_aso_tab_auth'), null)
  login(current, 'valid')
  current.http.defaults.adapter = async config => { throw { config, response: { status: 403, data: { code: 'PERMISSION_DENIED' } } } }
  await assert.rejects(current.http.get('/test'))
  assert.equal(current.window.location.href, '')
  assert.ok(current.session.getItem('youou_aso_tab_auth'))
})
