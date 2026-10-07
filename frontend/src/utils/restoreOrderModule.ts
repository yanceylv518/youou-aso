import type { OrderModuleConfig } from '@/api/orderModules'

export function restoreOrderModuleId(
  source: { orderType: string; orderModuleId?: number | null; orderModuleName?: string | null },
  modules: OrderModuleConfig[]
): number | null {
  const candidates = modules.filter(module => module.enabled && module.orderType === source.orderType)
  if (source.orderModuleId != null) {
    return candidates.find(module => module.id === source.orderModuleId)?.id ?? null
  }
  const named = candidates.filter(module => module.moduleName === source.orderModuleName)
  if (named.length === 1) return named[0].id
  return candidates.length === 1 ? candidates[0].id : null
}
