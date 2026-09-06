import { createServer } from 'vite'

const allModuleNames = ['pricing', 'supportConfig', 'mailConfig', 'walletTypeConfig', 'regions', 'orderCreate', 'promotionPage', 'adminPromotion']
const allTargets = { 'es-ES': 'es', 'pt-PT': 'pt', 'ru-RU': 'ru' }
const requestedLocale = process.argv[2]
const residualMode = process.argv[3] === 'residual'
const requestedModules = residualMode ? undefined : process.argv[3]?.split(',').filter(Boolean)
const moduleNames = requestedModules?.length ? requestedModules : allModuleNames
const targets = requestedLocale ? { [requestedLocale]: allTargets[requestedLocale] } : allTargets

if (moduleNames.some((name) => !allModuleNames.includes(name))) throw new Error('Unknown locale module requested')
if (Object.values(targets).some((target) => !target)) throw new Error('Unknown target locale requested')
const technicalOnly = /^(?:SMTP|SSL|STARTTLS|App|App Store|Google Play|iPad Store|Youou-ASO Support|keyword-import-template\.xlsx|smtp\.gmail\.com)$/
const placeholderPattern = /\{[^}]+\}|https?:\/\/\S+|\.xlsx|\.csv/g

function clone(value) {
  if (Array.isArray(value)) return value.map(clone)
  if (value && typeof value === 'object') return Object.fromEntries(Object.entries(value).map(([key, child]) => [key, clone(child)]))
  return value
}

function collectLeaves(value, output = []) {
  if (value && typeof value === 'object' && !Array.isArray(value)) {
    for (const child of Object.values(value)) collectLeaves(child, output)
  } else if (typeof value === 'string') output.push(value)
  return output
}

function replaceLeaves(value, translations, cursor) {
  if (value && typeof value === 'object' && !Array.isArray(value)) {
    return Object.fromEntries(Object.entries(value).map(([key, child]) => [key, replaceLeaves(child, translations, cursor)]))
  }
  if (typeof value !== 'string') return value
  return translations[cursor.index++]
}

function flatten(value, prefix = '', output = {}) {
  if (value && typeof value === 'object' && !Array.isArray(value)) {
    for (const [key, child] of Object.entries(value)) flatten(child, prefix ? `${prefix}.${key}` : key, output)
  } else output[prefix] = value
  return output
}

function setPath(target, path, value) {
  const parts = path.split('.')
  let cursor = target
  for (const part of parts.slice(0, -1)) cursor = cursor[part]
  cursor[parts.at(-1)] = value
}

function maskPlaceholders(text) {
  const placeholders = []
  const masked = text.replace(placeholderPattern, (value) => {
    const token = `ZXQPH${placeholders.length}QXZ`
    placeholders.push(value)
    return token
  })
  return { masked, placeholders }
}

function restorePlaceholders(text, placeholders) {
  return placeholders.reduce((result, value, index) => result.replace(new RegExp(`ZXQPH\\s*${index}\\s*QXZ`, 'gi'), value), text)
}

async function translateText(text, target) {
  if (!text.trim() || technicalOnly.test(text)) return text
  const { masked, placeholders } = maskPlaceholders(text)
  const url = `https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=${target}&dt=t&q=${encodeURIComponent(masked)}`
  let lastError
  for (let attempt = 0; attempt < 4; attempt += 1) {
    try {
      const response = await fetch(url)
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const payload = await response.json()
      return restorePlaceholders(payload[0].map((part) => part[0]).join(''), placeholders)
    } catch (error) {
      lastError = error
      await new Promise((resolve) => setTimeout(resolve, 400 * (attempt + 1)))
    }
  }
  throw lastError
}

async function translateAll(values, target) {
  const unique = [...new Set(values)]
  const cache = new Map()
  let cursor = 0
  const workers = Array.from({ length: 10 }, async () => {
    while (cursor < unique.length) {
      const source = unique[cursor++]
      cache.set(source, await translateText(source, target))
    }
  })
  await Promise.all(workers)
  return values.map((value) => cache.get(value))
}

const server = await createServer({ server: { middlewareMode: true } })
try {
  const { localeMessages } = await server.ssrLoadModule('/src/i18n/index.ts')
  if (residualMode) {
    const locale = requestedLocale
    const target = targets[locale]
    const englishFlat = flatten(localeMessages['en-US'])
    const localized = clone(localeMessages[locale])
    const localizedFlat = flatten(localized)
    const allowedIdentical = new Set([
      'app.name', 'common.english', 'common.portuguese', 'common.spanish',
      'supportConfig.defaultServiceName', 'mailConfig.startTlsEnabled', 'mailConfig.sslEnabled', 'mailConfig.smtpStatus',
      'orderCreate.app', 'orderCreate.keywordTemplateFilename', 'dynamicPromotion.chinaRegion',
      'orderModulesAdmin.chinaRegion', 'applications.identifierShort', 'orderCreate.total'
    ])
    const paths = Object.keys(englishFlat).filter((path) => !allowedIdentical.has(path)
      && typeof englishFlat[path] === 'string' && /[A-Za-z]{3}/.test(englishFlat[path])
      && localizedFlat[path] === englishFlat[path])
    const translations = await translateAll(paths.map((path) => englishFlat[path]), target)
    paths.forEach((path, index) => setPath(localized, path, translations[index]))
    const topLevels = [...new Set(paths.map((path) => path.split('.')[0]))]
    process.stdout.write(JSON.stringify({ [locale]: Object.fromEntries(topLevels.map((key) => [key, localized[key]])) }))
    process.exitCode = 0
  } else {
  const englishModules = Object.fromEntries(moduleNames.map((name) => [name, clone(localeMessages['en-US'][name])]))
  const leaves = collectLeaves(englishModules)
  const output = {}
  for (const [locale, target] of Object.entries(targets)) {
    const translations = await translateAll(leaves, target)
    if (translations.length !== leaves.length || translations.some((value) => typeof value !== 'string' || !value.trim())) throw new Error(`${locale} translation output is incomplete`)
    output[locale] = replaceLeaves(englishModules, translations, { index: 0 })
  }
  process.stdout.write(JSON.stringify(output))
  }
} finally {
  await server.close()
}
