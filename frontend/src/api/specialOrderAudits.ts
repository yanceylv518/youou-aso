import { http, type ApiResponse, type PageResult } from './http'
import type { StoreType } from './applications'
import type { Order, OrderType } from './orders'

export type SpecialAuditStatus = 'PENDING_REVIEW' | 'APPROVED_WAIT_SUBMIT' | 'CANCELLED' | 'SUBMITTED'

export interface SpecialOrderAudit {
  id: number
  auditNo: string
  customerId: number
  customerAppId: number
  orderType: OrderType
  storeType: StoreType
  regionCode: string
  appIdentifier: string
  appName: string
  appIconUrl: string | null
  requestedContent: string
  contactType: string | null
  contactValue: string | null
  items: SpecialOrderAuditItem[]
  negotiatedContent: string | null
  negotiatedPrice: number | string | null
  status: SpecialAuditStatus
  reviewedByAdminId: number | null
  reviewedAt: string | null
  cancelReason: string | null
  submittedOrderId: number | null
  submittedAt: string | null
  createdAt: string | null
}

export interface SpecialOrderAuditItem {
  regionCode: string
  keyword: string
  targetRank: number | null
  coverageNote: string | null
}

export interface SubmitSpecialAuditPayload {
  customerAppId: number
  regionCode?: string | null
  orderType: OrderType
  requestedContent: string
  contactType?: string | null
  contactValue?: string | null
  items?: SpecialOrderAuditItem[]
}

export interface ReviewSpecialAuditPayload {
  negotiatedContent: string
  negotiatedPrice: number
}

export async function getCustomerSpecialAudits() {
  const response = await http.get<ApiResponse<SpecialOrderAudit[]>>('/customer/special-order-audits')
  return response.data.data
}

export async function getCustomerSpecialAuditsPage(params: { page: number; pageSize: number }) {
  const response = await http.get<ApiResponse<PageResult<SpecialOrderAudit>>>('/customer/special-order-audits', { params })
  return response.data.data
}

export async function submitSpecialAudit(payload: SubmitSpecialAuditPayload) {
  const response = await http.post<ApiResponse<SpecialOrderAudit>>('/customer/special-order-audits', payload)
  return response.data.data
}

export async function submitApprovedSpecialAudit(id: number) {
  const response = await http.post<ApiResponse<Order>>(`/customer/special-order-audits/${id}/submit`)
  return response.data.data
}

export async function getAdminSpecialAudits() {
  const response = await http.get<ApiResponse<SpecialOrderAudit[]>>('/admin/special-order-audits')
  return response.data.data
}

export async function getAdminSpecialAuditsPage(params: { page: number; pageSize: number }) {
  const response = await http.get<ApiResponse<PageResult<SpecialOrderAudit>>>('/admin/special-order-audits', { params })
  return response.data.data
}

export async function reviewSpecialAudit(id: number, payload: ReviewSpecialAuditPayload) {
  const response = await http.post<ApiResponse<SpecialOrderAudit>>(`/admin/special-order-audits/${id}/review`, payload)
  return response.data.data
}

export async function cancelSpecialAudit(id: number, reason: string) {
  const response = await http.post<ApiResponse<SpecialOrderAudit>>(`/admin/special-order-audits/${id}/cancel`, { reason })
  return response.data.data
}
