import { createI18n } from 'vue-i18n'
import zhCN from './locales/zh-CN'
import enUS from './locales/en-US'

export type AppLocale = 'zh-CN' | 'en-US'

export const LOCALE_STORAGE_KEY = 'youou_aso_locale'

const supportedLocales: AppLocale[] = ['zh-CN', 'en-US']

const getInitialLocale = (): AppLocale => {
  if (typeof window === 'undefined') {
    return 'zh-CN'
  }

  const storedLocale = window.localStorage.getItem(LOCALE_STORAGE_KEY) as AppLocale | null
  if (storedLocale && supportedLocales.includes(storedLocale)) {
    return storedLocale
  }

  return window.navigator.language.startsWith('en') ? 'en-US' : 'zh-CN'
}

const initialLocale = getInitialLocale()

if (typeof document !== 'undefined') {
  document.documentElement.lang = initialLocale === 'zh-CN' ? 'zh-CN' : 'en'
}

export const i18n = createI18n({
  legacy: false,
  locale: initialLocale,
  fallbackLocale: 'en-US',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS
  }
})
