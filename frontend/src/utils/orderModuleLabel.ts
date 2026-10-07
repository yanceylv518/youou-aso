import { localizedServiceName } from './moduleLocalization'

export interface OrderModuleLabelSource {
  orderModuleName?: string | null
  orderModuleNames?: Partial<Record<string, string>>
  orderType?: string | null
}

// Use the same names and fallback policy in lists, details and exports.
export function orderModuleLabel(order: OrderModuleLabelSource): string {
  return localizedServiceName(order.orderModuleName, order.orderModuleNames, order.orderType)
}
