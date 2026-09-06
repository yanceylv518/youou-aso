import { http, type ApiResponse } from './http'
import type { OrderType } from './orders'

export type PriceCode =
  | 'KEYWORD_INSTALL'
  | 'DOWNLOAD'
  | 'RATING_5'
  | 'RATING_4'
  | 'REVIEW_5'
  | 'REVIEW_4'

export interface PricingConfig {
  code: PriceCode
  unitPrice: string | number
  chinaUnitPrice: string | number
  enabled: boolean
}

export interface UpdatePricingPayload {
  items: Array<{
    code: PriceCode
    unitPrice: string | number
    chinaUnitPrice: string | number
  }>
}

export interface OrderTypeRegionPricing {
  orderType: OrderType
  allowedRegionCodes: string[]
  regionPrices: Partial<Record<PriceCode, Record<string, string | number>>>
}

export async function getPricingConfig() {
  const response = await http.get<ApiResponse<PricingConfig[]>>('/admin/pricing')
  return response.data.data
}

export async function getCustomerPricingConfig() {
  const response = await http.get<ApiResponse<PricingConfig[]>>('/customer/pricing')
  return response.data.data
}

export async function updatePricingConfig(payload: UpdatePricingPayload) {
  const response = await http.put<ApiResponse<PricingConfig[]>>('/admin/pricing', payload)
  return response.data.data
}

export async function getAdminOrderTypeRegionPricing() {
  const response = await http.get<ApiResponse<OrderTypeRegionPricing[]>>('/admin/pricing/regions')
  return response.data.data
}

export async function getCustomerOrderTypeRegionPricing() {
  const response = await http.get<ApiResponse<OrderTypeRegionPricing[]>>('/customer/pricing/regions')
  return response.data.data
}

export async function updateOrderTypeRegionPricing(payload: OrderTypeRegionPricing[]) {
  const response = await http.put<ApiResponse<OrderTypeRegionPricing[]>>('/admin/pricing/regions', payload)
  return response.data.data
}
