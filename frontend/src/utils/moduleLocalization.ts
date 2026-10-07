import serviceAliases from '@/i18n/serviceNameAliases.json'
import { i18n, type AppLocale } from '@/i18n'
import type { OrderModuleConfig } from '@/api/orderModules'

const suffixes: Record<string, string> = { 'zh-CN': '', 'en-US': 'En', 'ru-RU': 'Ru', 'pt-PT': 'Pt', 'es-ES': 'Es' }
const variants: Record<string, string[]> = {
  'zh-CN': ['普通', '高级'], 'en-US': ['Standard', 'Advanced'],
  'ru-RU': ['Стандартный', 'Расширенный'], 'pt-PT': ['Padrão', 'Avançado'], 'es-ES': ['Estándar', 'Avanzado']
}
const normalized = (value: string) => value.replace(/[（]/g, '(').replace(/[）]/g, ')').replace(/\s+/g, '').toLowerCase()

export function localizedServiceName(source: string | null | undefined, names: Partial<Record<string, string>> = {}, type?: string | null, locale: string = i18n.global.locale.value): string {
  const selected = names[locale]?.trim()
  const original = source?.trim() || names['zh-CN']?.trim() || ''
  const match = normalized(original).match(/^关键词安装(?:\((普通|标准|高级)\))?$/)
  const key = `ordersPage.types.${type}`
  const base = i18n.global.te(key, locale as AppLocale) ? i18n.global.t(key, {}, { locale: locale as AppLocale }) : ''
  const knownAliases = serviceAliases[type as keyof typeof serviceAliases] || []
  const knownSelected = !selected || knownAliases.some(alias => normalized(alias) === normalized(selected))
  if (match && type === 'KEYWORD_INSTALL') {
    const variant = match[1] ? (match[1] === '高级' ? 1 : 0) : -1
    // Only repair known legacy labels; administrator-defined marketing names stay intact.
    const aliases = /^(keywordinstalls?|keywordinstallations?|keyinstallation)(\((general|standard|advanced)\))?$/
    const legacyVariant = normalized(selected || '').match(/^(.*)\((general|standard|advanced|geral|padrão|avançado|estándar|avanzado|общий|стандартный|расширенный|продвинутый|普通|高级)\)$/)
    const knownVariant = legacyVariant && knownAliases.some(alias => normalized(alias) === legacyVariant[1])
    const isKnown = knownVariant || knownSelected || selected === original || aliases.test(normalized(selected || ''))
    if (isKnown) return base + (variant < 0 ? '' : locale === 'zh-CN' ? `（${variants[locale]![variant]}）` : ` (${(variants[locale] || variants['en-US'])![variant]})`)
  }
  if (knownSelected && knownAliases.some(alias => normalized(alias) === normalized(original))) return base
  return selected || names['en-US']?.trim() || original || base || '—'
}

export function localizedModule(module: OrderModuleConfig, field: 'moduleName' | 'moduleDescription', locale: string = i18n.global.locale.value): string {
  const values = Object.fromEntries(Object.entries(suffixes).map(([language, suffix]) => [language, String(module[`${field}${suffix}` as keyof OrderModuleConfig] || '').trim()]))
  return field === 'moduleName' ? localizedServiceName(module.moduleName, values, module.orderType, locale) : values[locale] || values['en-US'] || values['zh-CN'] || ''
}
