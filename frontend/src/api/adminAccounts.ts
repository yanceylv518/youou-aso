import { http, type ApiResponse } from './http'
import type { AccountStatus } from './customers'
import type { AdminRole } from '@/stores/auth'

export interface AdminAccount {
  id: number
  username: string
  email: string
  roleCode: AdminRole
  roleIds: number[]
  roleKeys: string[]
  status: AccountStatus
  forcePasswordChange: boolean
  preferredLocale: string
  lastLoginAt: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateAdminAccountPayload {
  username: string
  email: string
  password: string
  roleIds: number[]
}

export async function getAdminAccounts() {
  const response = await http.get<ApiResponse<AdminAccount[]>>('/admin/admin-accounts')
  return response.data.data
}

export async function createAdminAccount(payload: CreateAdminAccountPayload) {
  const response = await http.post<ApiResponse<AdminAccount>>('/admin/admin-accounts', payload)
  return response.data.data
}

export async function updateAdminAccountStatus(id: number, status: AccountStatus) {
  const response = await http.put<ApiResponse<AdminAccount>>(`/admin/admin-accounts/${id}/status`, { status })
  return response.data.data
}

export async function updateAdminAccountRoles(id: number, roleIds: number[]) {
  const response = await http.put<ApiResponse<AdminAccount>>(`/admin/admin-accounts/${id}/roles`, { roleIds })
  return response.data.data
}

export async function getAdminAccountPermissions(id: number) {
  const response = await http.get<ApiResponse<number[]>>(`/admin/admin-accounts/${id}/permissions`)
  return response.data.data
}

export async function updateAdminAccountPermissions(id: number, menuIds: number[]) {
  const response = await http.put<ApiResponse<number[]>>(`/admin/admin-accounts/${id}/permissions`, { menuIds })
  return response.data.data
}
