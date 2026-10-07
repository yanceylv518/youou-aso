import { i18n } from '@/i18n'

export interface OrderModuleLabelSource {
  orderModuleName?: string | null
  orderModuleNames?: Partial<Record<string, string>>
  orderType?: string | null
}

// Module translations follow the selected language; old orders fall back to the localized task type.
export function orderModuleLabel(order: OrderModuleLabelSource): string {
  const locale = i18n.global.locale.value
  const translated = order.orderModuleNames?.[locale]?.trim()
  if (translated) return translated
  if (locale === 'zh-CN' && order.orderModuleName?.trim()) return order.orderModuleName.trim()
  const key = `ordersPage.types.${order.orderType}`
  return i18n.global.te(key) ? i18n.global.t(key) : '—'
}
