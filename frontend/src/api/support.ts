import { http, type ApiResponse } from './http'

export interface CustomerServiceConfig {
  serviceName: string
  qrCodeUrl: string | null
  contactHint: string | null
  enabled: boolean
  updatedAt: string | null
}

export interface UpdateCustomerServiceConfigPayload {
  serviceName: string
  qrCodeUrl?: string
  contactHint?: string
  enabled: boolean
}

export interface MailConfig {
  smtpHost: string | null
  smtpPort: number | null
  username: string | null
  fromAddress: string | null
  smtpAuth: boolean
  startTlsEnabled: boolean
  sslEnabled: boolean
  passwordConfigured: boolean
  orderNotificationRecipients: string | null
  orderNotificationEnabled: boolean
  updatedAt: string | null
}

export interface UpdateMailConfigPayload {
  smtpHost?: string
  smtpPort: number
  username?: string
  password?: string
  keepExistingPassword: boolean
  fromAddress?: string
  smtpAuth: boolean
  startTlsEnabled: boolean
  sslEnabled: boolean
  orderNotificationRecipients?: string
  orderNotificationEnabled: boolean
}

export async function getCustomerServiceConfig() {
  const response = await http.get<ApiResponse<CustomerServiceConfig>>('/customer/support/customer-service')
  return response.data.data
}

export async function getAdminCustomerServiceConfig() {
  const response = await http.get<ApiResponse<CustomerServiceConfig>>('/admin/support/customer-service')
  return response.data.data
}

export async function updateAdminCustomerServiceConfig(payload: UpdateCustomerServiceConfigPayload) {
  const response = await http.put<ApiResponse<CustomerServiceConfig>>('/admin/support/customer-service', payload)
  return response.data.data
}

export async function getMailConfig() {
  const response = await http.get<ApiResponse<MailConfig>>('/admin/support/mail')
  return response.data.data
}

export async function updateMailConfig(payload: UpdateMailConfigPayload) {
  const response = await http.put<ApiResponse<MailConfig>>('/admin/support/mail', payload)
  return response.data.data
}
