import { http, type ApiResponse } from './http'

export interface AdminMarketRegion {
  code: string
  nameZh: string
  nameEn: string
  enabled: boolean
  supportsAppStore: boolean
  supportsGooglePlay: boolean
  supportsIpadStore: boolean
  sortOrder: number
}

export interface UpdateMarketRegionPayload {
  enabled: boolean
  supportsAppStore: boolean
  supportsGooglePlay: boolean
  supportsIpadStore: boolean
}

export async function getAdminRegions() {
  const response = await http.get<ApiResponse<AdminMarketRegion[]>>('/admin/regions')
  return response.data.data
}

export async function updateAdminRegion(code: string, payload: UpdateMarketRegionPayload) {
  const response = await http.put<ApiResponse<AdminMarketRegion>>(`/admin/regions/${code}`, payload)
  return response.data.data
}
