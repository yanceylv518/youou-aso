import { http, type ApiResponse, type PageResult } from './http'

export type StoreType = 'APP_STORE' | 'GOOGLE_PLAY' | 'IPAD_STORE'

export interface MarketRegion {
  code: string
  nameZh: string
  nameEn: string
  supportsAppStore: boolean
  supportsGooglePlay: boolean
  supportsIpadStore: boolean
}

export interface CustomerApp {
  id: number
  customerId: number
  storeType: StoreType
  regionCode: string
  regionCodes: string[]
  appIdentifier: string
  appName: string
  appIconUrl: string | null
  bundleId: string | null
  externalAppId: string | null
  category: string | null
  customerUsername: string | null
  customerEmail: string | null
  status: string
  verifiedAt: string
  createdAt: string
}

export interface CreateCustomerAppPayload {
  storeType: StoreType
  regionCode: string
  appIdentifier: string
  category?: string | null
  appName?: string | null
  appIconUrl?: string | null
}

export interface AdminCreateCustomerAppPayload extends CreateCustomerAppPayload {
  customerId: number
}

export interface CustomerAppQuery {
  keyword?: string
  storeType?: StoreType | ''
  regionCode?: string
  page?: number
  pageSize?: number
}

export interface StoreAppSearchResult {
  storeType: StoreType
  regionCode: string
  appIdentifier: string
  appName: string
  appIconUrl: string | null
  bundleId: string | null
  externalAppId: string | null
  category: string | null
  developerName: string | null
}

export interface AppIconUploadResult {
  url: string
}

export async function getEnabledRegions(storeType?: StoreType) {
  const response = await http.get<ApiResponse<MarketRegion[]>>('/regions/enabled', {
    params: storeType ? { storeType } : undefined
  })
  return response.data.data
}

export async function getCustomerApps(params?: CustomerAppQuery) {
  const response = await http.get<ApiResponse<CustomerApp[]>>('/customer/apps', { params: cleanQuery(params) })
  return response.data.data
}

export async function getCustomerAppsPage(params: CustomerAppQuery) {
  const response = await http.get<ApiResponse<PageResult<CustomerApp>>>('/customer/apps', { params: cleanQuery(params) })
  return response.data.data
}

export async function createCustomerApp(payload: CreateCustomerAppPayload) {
  const response = await http.post<ApiResponse<CustomerApp>>('/customer/apps', payload)
  return response.data.data
}

export async function createAdminAppForCustomer(payload: AdminCreateCustomerAppPayload) {
  const response = await http.post<ApiResponse<CustomerApp>>('/admin/apps', payload)
  return response.data.data
}

export async function uploadAppIcon(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await http.post<ApiResponse<AppIconUploadResult>>('/app-icons', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  return response.data.data
}

export async function deleteCustomerApp(id: number) {
  const response = await http.delete<ApiResponse<void>>(`/customer/apps/${id}`)
  return response.data.data
}

export async function searchStoreApps(params: {
  storeType: StoreType
  regionCode: string
  keyword: string
  limit?: number
}) {
  const response = await http.get<ApiResponse<StoreAppSearchResult[]>>('/customer/apps/search', {
    params: {
      ...params,
      limit: params.limit ?? 10
    }
  })
  return response.data.data
}

export async function searchAdminStoreApps(params: {
  storeType: StoreType
  regionCode: string
  keyword: string
  limit?: number
}) {
  const response = await http.get<ApiResponse<StoreAppSearchResult[]>>('/admin/apps/search', {
    params: {
      ...params,
      limit: params.limit ?? 10
    }
  })
  return response.data.data
}

export async function getAdminApps(params?: CustomerAppQuery) {
  const response = await http.get<ApiResponse<CustomerApp[]>>('/admin/apps', { params: cleanQuery(params) })
  return response.data.data
}

export async function getAdminAppsPage(params: CustomerAppQuery) {
  const response = await http.get<ApiResponse<PageResult<CustomerApp>>>('/admin/apps', { params: cleanQuery(params) })
  return response.data.data
}

export async function deleteAdminApp(id: number) {
  const response = await http.delete<ApiResponse<void>>(`/admin/apps/${id}`)
  return response.data.data
}

function cleanQuery(params?: CustomerAppQuery) {
  if (!params) return undefined
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== '')
  )
}
