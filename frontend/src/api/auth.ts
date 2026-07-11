import { http, type ApiResponse } from './http'
import type { AccountType, AdminRole } from '@/stores/auth'

export interface LoginPayload {
  account: string
  password: string
}

export interface RegisterPayload {
  username: string
  email: string
  password: string
}

export interface LoginResult {
  token: string
  accountId: number
  username: string
  email: string
  accountType: AccountType
  roleCode: AdminRole | ''
  forcePasswordChange: boolean
  preferredLocale: string
  roleKeys: string[]
  menuCodes: string[]
  permissions: string[]
}

export interface RegisterResult {
  accountId: number
  username: string
  email: string
}

export interface CurrentAccountResult {
  accountId: number
  username: string
  email: string
  accountType: AccountType
  roleCode: AdminRole | ''
  forcePasswordChange: boolean
  preferredLocale: string
  roleKeys: string[]
  menuCodes: string[]
  permissions: string[]
}

export interface ChangePasswordPayload {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

export interface RequestPasswordResetCodePayload {
  email: string
}

export interface ConfirmPasswordResetPayload {
  email: string
  code: string
  newPassword: string
  confirmPassword: string
}

export async function login(payload: LoginPayload) {
  const response = await http.post<ApiResponse<LoginResult>>('/auth/login', payload)
  return response.data.data
}

export async function registerCustomer(payload: RegisterPayload) {
  const response = await http.post<ApiResponse<RegisterResult>>('/auth/register', payload)
  return response.data.data
}

export async function getCurrentAccount() {
  const response = await http.get<ApiResponse<CurrentAccountResult>>('/auth/me')
  return response.data.data
}

export async function changePassword(payload: ChangePasswordPayload) {
  const response = await http.put<ApiResponse<void>>('/auth/password', payload)
  return response.data.data
}

export async function requestPasswordResetCode(payload: RequestPasswordResetCodePayload) {
  const response = await http.post<ApiResponse<void>>('/auth/password-reset/code', payload)
  return response.data.data
}

export async function confirmPasswordReset(payload: ConfirmPasswordResetPayload) {
  const response = await http.post<ApiResponse<void>>('/auth/password-reset/confirm', payload)
  return response.data.data
}