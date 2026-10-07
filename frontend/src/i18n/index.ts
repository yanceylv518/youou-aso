import { applyTerminology } from './terminology'
import { visualMessages } from './visual'
import { createI18n } from 'vue-i18n'
import zhCN from './locales/zh-CN'
import enUS from './locales/en-US'
import ruRU from './locales/ru-RU'
import ptPT from './locales/pt-PT'
import esES from './locales/es-ES'
import { validateLocaleMessages } from './validate'
import { readBrowserStorage } from '@/utils/browserStorage'

export type AppLocale = 'zh-CN' | 'en-US' | 'ru-RU' | 'pt-PT' | 'es-ES'

export interface LocaleOption {
  code: AppLocale
  htmlLang: string
  nativeLabel: string
  labelKey: string
}

export const LOCALE_STORAGE_KEY = 'youou_aso_locale'
export const DEFAULT_LOCALE: AppLocale = 'en-US'

export const localeOptions: LocaleOption[] = [
  { code: 'zh-CN', htmlLang: 'zh-CN', nativeLabel: '中文', labelKey: 'common.chinese' },
  { code: 'en-US', htmlLang: 'en', nativeLabel: 'English', labelKey: 'common.english' },
  { code: 'ru-RU', htmlLang: 'ru', nativeLabel: 'Русский', labelKey: 'common.russian' },
  { code: 'pt-PT', htmlLang: 'pt', nativeLabel: 'Português', labelKey: 'common.portuguese' },
  { code: 'es-ES', htmlLang: 'es', nativeLabel: 'Español', labelKey: 'common.spanish' }
]
export const supportedLocales: AppLocale[] = localeOptions.map(({ code }) => code)
export const localeHtmlLang = Object.fromEntries(localeOptions.map(({ code, htmlLang }) => [code, htmlLang])) as Record<AppLocale, string>

const getInitialLocale = (): AppLocale => {
  if (typeof window === 'undefined') {
    return DEFAULT_LOCALE
  }

  const storedLocale = readBrowserStorage(LOCALE_STORAGE_KEY) as AppLocale | null
  if (storedLocale && supportedLocales.includes(storedLocale)) {
    return storedLocale
  }

  return DEFAULT_LOCALE
}

const initialLocale = getInitialLocale()

if (typeof document !== 'undefined') {
  document.documentElement.lang = localeHtmlLang[initialLocale]
}

export const localeMessages = { 'zh-CN': { ...zhCN, visual: visualMessages['zh-CN'] }, 'en-US': { ...enUS, visual: visualMessages['en-US'] }, 'ru-RU': { ...ruRU, visual: visualMessages['ru-RU'] }, 'pt-PT': { ...ptPT, visual: visualMessages['pt-PT'] }, 'es-ES': { ...esES, visual: visualMessages['es-ES'] } }
applyTerminology(localeMessages)
validateLocaleMessages(localeMessages)

export const i18n = createI18n({
  legacy: false,
  locale: initialLocale,
  fallbackLocale: 'en-US',
  messages: localeMessages
})
