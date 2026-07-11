import { http, type ApiResponse } from './http'

export type RoleStatus = 'ENABLED' | 'DISABLED'
export type MenuType = 'CATALOG' | 'MENU' | 'BUTTON'

export interface AdminMenu {
  id: number
  parentId: number | null
  menuType: MenuType
  menuCode: string | null
  nameZh: string
  nameEn: string
  routePath: string | null
  permissionCode: string | null
  icon: string | null
  sortOrder: number
  visible: boolean
  status: RoleStatus
}

export interface AdminRoleConfig {
  id: number
  roleKey: string
  roleName: string
  status: RoleStatus
  sortOrder: number
  builtIn: boolean
  remark: string | null
  menuIds: number[]
  permissionCodes: string[]
  createdAt: string
  updatedAt: string
}

export interface SaveAdminRolePayload {
  roleKey?: string
  roleName: string
  status: RoleStatus
  sortOrder: number
  remark?: string
  menuIds: number[]
}

export async function getRoleMenus() {
  const response = await http.get<ApiResponse<AdminMenu[]>>('/admin/roles/menus')
  return response.data.data
}

export async function getAdminRoles() {
  const response = await http.get<ApiResponse<AdminRoleConfig[]>>('/admin/roles')
  return response.data.data
}

export async function createAdminRole(payload: SaveAdminRolePayload) {
  const response = await http.post<ApiResponse<AdminRoleConfig>>('/admin/roles', payload)
  return response.data.data
}

export async function updateAdminRole(id: number, payload: SaveAdminRolePayload) {
  const response = await http.put<ApiResponse<AdminRoleConfig>>(`/admin/roles/${id}`, payload)
  return response.data.data
}