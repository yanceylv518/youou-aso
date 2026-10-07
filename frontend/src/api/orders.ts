import { listReviewAttachments, type ReviewAttachment } from './reviewAttachments'
import { http, type ApiResponse, type PageResult } from './http'
import type { StoreType } from './applications'
import type { SpecialOrderAudit } from './specialOrderAudits'

export type OrderType = 'KEYWORD_INSTALL' | 'DOWNLOAD' | 'RATING' | 'REVIEW' | 'RANK_GUARANTEE' | 'CHART_RANK_GUARANTEE' | 'KEYWORD_COVERAGE'
export type OrderStatus =
  | 'PENDING_PAYMENT'
  | 'PENDING_CONFIRM'
  | 'PENDING_EXECUTION'
  | 'EXECUTING'
  | 'PAUSED'
  | 'COMPLETED'
  | 'CANCELLED'
export type OrderListStatus = OrderStatus | 'PENDING_REVIEW' | 'APPROVED_WAIT_SUBMIT' | 'SUBMITTED'

export interface OrderItem {
  id: number
  itemType: string
  itemName: string | null
  regionCode: string | null
  quantity: number | null
  completedQuantity: number | null
  unitPrice: number | null
  amount: number | null
  metadataJson: string | null
}

export interface OrderEvent {
  reason?: string | null
  id: number
  eventType: 'PAUSED' | 'UPDATED' | 'RESUMED' | string
  quantityBefore: number | null
  quantityAfter: number | null
  completedBefore: number | null
  completedAfter: number | null
  amountBefore: number | null
  amountAfter: number | null
  createdByAdminId: number | null
  createdAt: string
}
export interface OrderCommentDetail {
  regionCode: string
  starLevel: number
  commentTitle: string
  commentContent: string
}

export interface Order {
  id: number
  orderNo: string
  customerId: number
  customerUsername: string | null
  customerEmail: string | null
  customerAppId: number
  sourceAuditId: number | null
  orderType: OrderType
  orderModuleId: number | null
  orderModuleName: string | null
  orderModuleNames?: Partial<Record<import('@/i18n').AppLocale, string>>
  storeType: StoreType
  regionCode: string
  appIdentifier: string
  appName: string
  appIconUrl: string | null
  status: OrderStatus
  scheduledStartAt?: string | null
  orderStartDate: string
  orderEndDate: string
  executionHours: number | null
  totalDays: number | null
  quantity: number | null
  unitPrice: number | null
  totalAmount: number
  refundAmount?: number | null
  expectedCompletedAt: string | null
  confirmedAt: string | null
  executedAt: string | null
  completedAt: string | null
  createdAt: string | null
  items: OrderItem[]
  reviewAttachments?: ReviewAttachment[]
  commentDetails: OrderCommentDetail[]
  events: OrderEvent[]
}

export interface OrderQuery {
  storeType?: StoreType | ''
  status?: OrderStatus | ''
  keyword?: string
  customerId?: number | null
  customerAppId?: number | null
  regionCode?: string
  orderType?: OrderType | ''
  specialOrder?: boolean | null
  orderDateFrom?: string
  orderDateTo?: string
  createdDateFrom?: string
  createdDateTo?: string
  page?: number
  pageSize?: number
}

export interface CreateOrderPayload {
  customerAppId: number
  regionCode?: string | null
  orderType: OrderType
  scheduledStartAt?: string | null
  startImmediately?: boolean
  startDate: string
  endDate: string
  executionHours?: number | null
  keywords?: string[]
  keywordItems?: Array<{
    regionCode?: string | null
    keyword: string
    quantity: number
  }>
  regionItems?: Array<{
    regionCode?: string | null
    dailyDownloadCount?: number | null
    rating5Count?: number | null
    rating4Count?: number | null
    review5Count?: number | null
    attachmentIds?: string[]
    review4Count?: number | null
  }>
  reviewDetails?: OrderCommentDetail[]
  dailyDownloadCount?: number | null
  rating5Count?: number | null
  rating4Count?: number | null
  review5Count?: number | null
  review4Count?: number | null
  orderModuleId?: number | null
}

export interface AdminCreateOrderPayload extends CreateOrderPayload {
  customerId: number
  contactType?: string | null
  contactValue?: string | null
  specialItems?: Array<{
    regionCode?: string | null
    keyword: string
    targetRank?: number | null
    coverageNote?: string | null
    unitPrice?: number | null
    executionDays?: number | null
  }>
  specialAmount?: number | null
}

export interface AdminCreateOrderResult {
  order: Order | null
  audit: SpecialOrderAudit | null
  paid: boolean
  waitPayment: boolean
}

export async function getCustomerOrders(params?: OrderQuery) {
  const response = await http.get<ApiResponse<Order[]>>('/customer/orders', { params: cleanQuery(params) })
  return response.data.data
}

export async function getCustomerOrdersPage(params: OrderQuery) {
  const response = await http.get<ApiResponse<PageResult<Order>>>('/customer/orders', { params: cleanQuery(params) })
  return response.data.data
}

export async function getCustomerOrder(id: number) {
  const response = await http.get<ApiResponse<Order>>(`/customer/orders/${id}`)
  const order = response.data.data
  order.reviewAttachments = order.orderType === 'REVIEW' ? await listReviewAttachments(id, false) : []
  return order
}

export async function createCustomerOrder(payload: CreateOrderPayload) {
  const response = await http.post<ApiResponse<Order>>('/customer/orders', payload)
  return response.data.data
}

export async function resubmitCustomerOrder(id: number, payload: CreateOrderPayload) {
  const response = await http.post<ApiResponse<Order>>(`/customer/orders/${id}/resubmit`, payload)
  return response.data.data
}

export async function payCustomerOrder(id: number) {
  const response = await http.post<ApiResponse<Order>>(`/customer/orders/${id}/pay`)
  return response.data.data
}

export async function editAdminOrder(id: number, payload: AdminCreateOrderPayload) {
  const response = await http.post<ApiResponse<Order>>(`/admin/orders/${id}/edit`, payload)
  return response.data.data
}

export async function createAdminOrderForCustomer(payload: AdminCreateOrderPayload) {
  const response = await http.post<ApiResponse<AdminCreateOrderResult>>('/admin/orders/create-for-customer', payload)
  return response.data.data
}

export async function getAdminOrders(params?: OrderQuery) {
  const response = await http.get<ApiResponse<Order[]>>('/admin/orders', { params: cleanQuery(params) })
  return response.data.data
}

export async function getAdminOrdersPage(params: OrderQuery) {
  const response = await http.get<ApiResponse<PageResult<Order>>>('/admin/orders', { params: cleanQuery(params) })
  return response.data.data
}

export async function getAdminOrder(id: number) {
  const response = await http.get<ApiResponse<Order>>(`/admin/orders/${id}`)
  const order = response.data.data
  order.reviewAttachments = order.orderType === 'REVIEW' ? await listReviewAttachments(id, true) : []
  return order
}

export async function confirmAdminOrder(id: number) {
  const response = await http.post<ApiResponse<Order>>(`/admin/orders/${id}/confirm`)
  return response.data.data
}

export async function batchConfirmAdminOrders(orderIds: number[]) {
  const response = await http.post<ApiResponse<Order[]>>('/admin/orders/batch-confirm', { orderIds })
  return response.data.data
}

export async function executeAdminOrder(id: number) {
  const response = await http.post<ApiResponse<Order>>(`/admin/orders/${id}/execute`)
  return response.data.data
}

export async function pauseAdminOrder(id: number) {
  const response = await http.post<ApiResponse<Order>>(`/admin/orders/${id}/pause`)
  return response.data.data
}

export async function adjustCompletedAdminOrder(id: number, items: Array<{ itemId: number; quantity: number; completedQuantity: number }>, reason: string) {
  const response = await http.post<ApiResponse<Order>>(`/admin/orders/${id}/completed-items`, { items, reason })
  return response.data.data
}

export async function closePausedAdminOrder(id: number, items: Array<{ itemId: number; quantity: number; completedQuantity: number }>) {
  const response = await http.post<ApiResponse<Order>>(`/admin/orders/${id}/close`, { items })
  return response.data.data
}

export async function updatePausedAdminOrder(id: number, items: Array<{ itemId: number; quantity: number; completedQuantity: number }>) {
  const response = await http.post<ApiResponse<Order>>(`/admin/orders/${id}/paused-items`, { items })
  return response.data.data
}

export async function resumeAdminOrder(id: number) {
  const response = await http.post<ApiResponse<Order>>(`/admin/orders/${id}/resume`)
  return response.data.data
}

export async function cancelAdminOrder(id: number, reason?: string) {
  const response = await http.post<ApiResponse<Order>>(`/admin/orders/${id}/cancel`, { reason })
  return response.data.data
}

export async function batchExecuteAdminOrders(orderIds: number[]) {
  const response = await http.post<ApiResponse<Order[]>>('/admin/orders/batch-execute', { orderIds })
  return response.data.data
}

export async function batchPauseAdminOrders(orderIds: number[]) {
  const response = await http.post<ApiResponse<Order[]>>('/admin/orders/batch-pause', { orderIds })
  return response.data.data
}

function cleanQuery(params?: OrderQuery) {
  if (!params) return undefined
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== '')
  )
}
