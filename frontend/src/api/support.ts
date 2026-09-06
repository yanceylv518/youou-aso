import { http, type ApiResponse } from './http'

export interface CustomerServiceConfig {
  serviceName: string
  qrCodeUrl: string | null
  contactHint: string | null
  email: string | null
  emailVisible: boolean
  phone: string | null
  phoneVisible: boolean
  teamsUrl: string | null
  telegramUrl: string | null
  telegramQrUrl: string | null
  telegramQrVisible: boolean
  wechatQrUrl: string | null
  wechatQrVisible: boolean
  whatsappUrl: string | null
  enabled: boolean
  updatedAt: string | null
}

export interface UpdateCustomerServiceConfigPayload {
  serviceName: string
  qrCodeUrl?: string
  contactHint?: string
  email?: string
  emailVisible: boolean
  phone?: string
  phoneVisible: boolean
  teamsUrl?: string
  telegramUrl?: string
  telegramQrUrl?: string
  telegramQrVisible: boolean
  wechatQrUrl?: string
  wechatQrVisible: boolean
  whatsappUrl?: string
  enabled: boolean
}

export interface CustomerServiceQrUploadResult {
  url: string
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

export interface HomeMetricsConfig {
  appsValue: string
  satisfactionValue: string
  experienceYears: number
  teamValue: string
  updatedAt: string | null
}

export type UpdateHomeMetricsPayload = Omit<HomeMetricsConfig, 'updatedAt'>

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

export async function uploadCustomerServiceQrImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await http.post<ApiResponse<CustomerServiceQrUploadResult>>(
    '/admin/support/customer-service/qr-image',
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  )
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

export async function getPublicHomeMetrics() {
  const response = await http.get<ApiResponse<HomeMetricsConfig>>('/public/home-metrics')
  return response.data.data
}

export async function getAdminHomeMetrics() {
  const response = await http.get<ApiResponse<HomeMetricsConfig>>('/admin/support/home-metrics')
  return response.data.data
}

export async function updateAdminHomeMetrics(payload: UpdateHomeMetricsPayload) {
  const response = await http.put<ApiResponse<HomeMetricsConfig>>('/admin/support/home-metrics', payload)
  return response.data.data
}
