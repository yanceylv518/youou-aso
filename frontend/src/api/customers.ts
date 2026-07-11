import { http, type ApiResponse, type PageResult } from './http'

export type AccountStatus = 'ENABLED' | 'DISABLED' | 'LOCKED'

export interface CustomerAccount {
  id: number
  username: string
  email: string
  status: AccountStatus
  forcePasswordChange: boolean
  preferredLocale: string
  balance: string | number
  frozenBalance: string | number
  lastLoginAt: string | null
  createdAt: string
  updatedAt: string
}

export async function getAdminCustomers(keyword?: string) {
  const response = await http.get<ApiResponse<CustomerAccount[]>>('/admin/customers', {
    params: keyword ? { keyword } : undefined
  })
  return response.data.data
}

export async function getAdminCustomersPage(params: { keyword?: string; page?: number; pageSize?: number }) {
  const response = await http.get<ApiResponse<PageResult<CustomerAccount>>>('/admin/customers', {
    params: Object.fromEntries(
      Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== '')
    )
  })
  return response.data.data
}

export async function updateAdminCustomerStatus(id: number, status: AccountStatus) {
  const response = await http.put<ApiResponse<CustomerAccount>>(`/admin/customers/${id}/status`, { status })
  return response.data.data
}
