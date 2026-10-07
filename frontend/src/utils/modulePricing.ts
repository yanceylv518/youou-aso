import type { OrderModuleConfig } from '@/api/orderModules'
import type { AdminMarketRegion } from '@/api/regions'
import type { OrderTypeRegionPricing } from '@/api/pricing'

export function availableModuleRegions(module: OrderModuleConfig | undefined, regions: AdminMarketRegion[]) {
  if (!module) return []
  return regions.filter(region => region.enabled &&
    module.storeTypes.some(store => store === 'APP_STORE' ? region.supportsAppStore : store === 'GOOGLE_PLAY' ? region.supportsGooglePlay : region.supportsIpadStore) &&
    !(['RATING', 'REVIEW'].includes(module.orderType) && region.code === 'CN'))
}

export function reconcileModulePricing(config: OrderTypeRegionPricing, module: OrderModuleConfig | undefined, regions: AdminMarketRegion[]) {
  const available = new Set(availableModuleRegions(module, regions).map(region => region.code))
  const allowedRegionCodes = config.allowedRegionCodes.filter(code => available.has(code))
  const removed = config.allowedRegionCodes.filter(code => !available.has(code))
  return {
    removed,
    config: {
      ...config,
      allowedRegionCodes,
      regionPrices: Object.fromEntries(Object.entries(config.regionPrices).map(([code, prices]) => [code,
        Object.fromEntries(Object.entries(prices ?? {}).filter(([region]) => allowedRegionCodes.includes(region)).map(([region, price]) => [region, Number(price)]))
      ]))
    } as OrderTypeRegionPricing
  }
}

/** Compare effective configuration, ignoring key order and cleared overrides. */
export function pricingSnapshot(config: OrderTypeRegionPricing) {
  return JSON.stringify({
    ...config,
    allowedRegionCodes: [...config.allowedRegionCodes].sort(),
    regionPrices: Object.fromEntries(Object.entries(config.regionPrices).sort(([a], [b]) => a.localeCompare(b)).map(([code, prices]) => [code,
      Object.fromEntries(Object.entries(prices ?? {}).filter(([, price]) => price != null).sort(([a], [b]) => a.localeCompare(b)))
    ]))
  })
}
