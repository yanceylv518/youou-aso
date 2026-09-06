import { http, type ApiResponse } from './http'

export interface AdminMarketRegion {
  code: string
  nameZh: string
  nameEn: string
  nameRu: string
  namePt: string
  nameEs: string
  enabled: boolean
  supportsAppStore: boolean
  supportsGooglePlay: boolean
  supportsIpadStore: boolean
  sortOrder: number
}

export interface SaveMarketRegionPayload {
  code?: string
  nameZh: string
  nameEn: string
  nameRu: string
  namePt: string
  nameEs: string
  enabled: boolean
  supportsAppStore: boolean
  supportsGooglePlay: boolean
  supportsIpadStore: boolean
  sortOrder: number
}

export async function getAdminRegions() {
  const response = await http.get<ApiResponse<AdminMarketRegion[]>>('/admin/regions')
  return response.data.data
}

export async function createAdminRegion(payload: SaveMarketRegionPayload) {
  const response = await http.post<ApiResponse<AdminMarketRegion>>('/admin/regions', payload)
  return response.data.data
}

export async function updateAdminRegion(code: string, payload: SaveMarketRegionPayload) {
  const response = await http.put<ApiResponse<AdminMarketRegion>>(`/admin/regions/${code}`, payload)
  return response.data.data
}
