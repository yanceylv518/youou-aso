import { http, type ApiResponse } from './http'
import type { OrderType } from './orders'
import type { AppLocale } from '@/i18n'

export interface OrderModuleConfig {
  id: number
  moduleName: string
  moduleNameEn: string
  moduleNameRu: string
  moduleNamePt: string
  moduleNameEs: string
  moduleDescription: string
  moduleDescriptionEn: string
  moduleDescriptionRu: string
  moduleDescriptionPt: string
  moduleDescriptionEs: string
  orderType: OrderType
  unitPrice: string | number | null
  chinaUnitPrice: string | number | null
  enabled: boolean
  sortOrder: number
}

export type SaveOrderModulePayload = Omit<OrderModuleConfig, 'id'>

export interface OrderModuleTranslation {
  name: string
  description: string
}

export async function getAdminOrderModules() {
  const response = await http.get<ApiResponse<OrderModuleConfig[]>>('/admin/order-modules')
  return response.data.data
}
export async function getCustomerOrderModules(orderType?: OrderType) {
  const response = await http.get<ApiResponse<OrderModuleConfig[]>>('/customer/order-modules', { params: { orderType } })
  return response.data.data
}
export async function createOrderModule(payload: SaveOrderModulePayload) {
  const response = await http.post<ApiResponse<OrderModuleConfig>>('/admin/order-modules', payload)
  return response.data.data
}
export async function updateOrderModule(id: number, payload: SaveOrderModulePayload) {
  const response = await http.put<ApiResponse<OrderModuleConfig>>(`/admin/order-modules/${id}`, payload)
  return response.data.data
}
export async function deleteOrderModule(id: number) {
  await http.delete(`/admin/order-modules/${id}`)
}

export async function translateOrderModule(payload: {
  sourceLocale: AppLocale
  moduleName: string
  moduleDescription: string
}) {
  const response = await http.post<ApiResponse<Partial<Record<AppLocale, OrderModuleTranslation>>>>(
    '/admin/order-modules/translate',
    payload,
  )
  return response.data.data
}
