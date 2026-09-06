import { createServer } from 'vite'

function flatten(value, prefix = '', output = {}) {
  if (value && typeof value === 'object' && !Array.isArray(value)) {
    for (const [key, child] of Object.entries(value)) flatten(child, prefix ? `${prefix}.${key}` : key, output)
  } else output[prefix] = value
  return output
}

const allowedIdentical = new Set([
  'app.name', 'common.english', 'common.portuguese', 'common.spanish',
  'supportConfig.defaultServiceName', 'mailConfig.startTlsEnabled', 'mailConfig.sslEnabled', 'mailConfig.smtpStatus',
  'orderCreate.app', 'orderCreate.keywordTemplateFilename', 'orderCreate.total',
  'dynamicPromotion.chinaRegion', 'orderModulesAdmin.chinaRegion', 'ordersPage.itemTypes.DOWNLOAD',
  'applications.identifierShort'
])

const server = await createServer({ server: { middlewareMode: true } })
try {
  const { localeMessages } = await server.ssrLoadModule('/src/i18n/index.ts')
  console.log('Locale runtime validation passed.')
  const english = localeMessages['en-US']
  const englishLeaves = flatten(english)
  const inheritedErrors = []
  for (const locale of ['ru-RU', 'pt-PT', 'es-ES']) {
    const inherited = Object.keys(english).filter((key) => key !== 'app' && localeMessages[locale][key] === english[key])
    console.log(`${locale} modules still inheriting English: ${inherited.join(', ') || 'none'}`)
    if (inherited.length) inheritedErrors.push(`${locale}: ${inherited.join(', ')}`)
    const localizedLeaves = flatten(localeMessages[locale])
    const englishLeavesRemaining = Object.keys(englishLeaves).filter((key) => !allowedIdentical.has(key)
      && typeof englishLeaves[key] === 'string' && /[A-Za-z]{3}/.test(englishLeaves[key])
      && localizedLeaves[key] === englishLeaves[key])
    console.log(`${locale} untranslated English leaves: ${englishLeavesRemaining.join(', ') || 'none'}`)
    if (englishLeavesRemaining.length) inheritedErrors.push(`${locale} leaves: ${englishLeavesRemaining.join(', ')}`)
  }
  if (inheritedErrors.length) throw new Error(`Locale modules still inherit English:\n${inheritedErrors.join('\n')}`)
} finally {
  await server.close()
}
