import { http, type ApiResponse, type PageResult } from './http'
import type { OrderType } from './orders'

export type WalletDirection = 'CREDIT' | 'DEBIT'

export type WalletTransactionType =
  | 'ORDER_DEDUCT'
  | 'ORDER_REFUND'
  | 'ADMIN_RECHARGE'
  | 'ADMIN_ADJUSTMENT'
  | 'ADMIN_REFUND'
  | 'ADMIN_GIFT'
  | 'ADMIN_DEDUCT'
  | 'DELIVERY'

export type AdminBalanceAdjustmentType = 'REFUND' | 'GIFT' | 'DEDUCT'

export interface WalletOverview {
  customerId: number
  balance: string | number
  frozenBalance: string | number
}

export interface WalletTransaction {
  id: number
  transactionNo: string
  customerId: number
  direction: WalletDirection
  transactionType: WalletTransactionType
  amount: string | number
  balanceBefore: string | number
  balanceAfter: string | number
  relatedOrderId?: number | null
  orderNo?: string | null
  orderType?: OrderType | null
  appName?: string | null
  remark?: string | null
  createdAt: string
}

export interface AdminRechargePayload {
  customerId: number
  amount: number
  remark?: string
}

export interface AdminBalanceAdjustmentPayload {
  customerId: number
  adjustmentType: AdminBalanceAdjustmentType
  amount: number
  remark: string
}

export interface WalletTransactionTypeConfig {
  transactionType: WalletTransactionType
  displayNameZh: string
  displayNameEn: string
  displayNameRu: string
  displayNamePt: string
  displayNameEs: string
  updatedAt: string | null
}

export interface WalletTransactionQuery {
  customerId?: number | null
  transactionType?: WalletTransactionType | ''
  direction?: WalletDirection | ''
  orderType?: OrderType | ''
  createdDateFrom?: string
  createdDateTo?: string
  limit?: number
  page?: number
  pageSize?: number
}

export interface UpdateWalletTransactionTypeConfigPayload {
  items: Array<{
    transactionType: WalletTransactionType
    displayNameZh: string
    displayNameEn: string
    displayNameRu: string
    displayNamePt: string
    displayNameEs: string
  }>
}

export async function getCustomerWallet() {
  const response = await http.get<ApiResponse<WalletOverview>>('/customer/wallet')
  return response.data.data
}

export async function getCustomerWalletTransactions(limit = 50) {
  const response = await http.get<ApiResponse<WalletTransaction[]>>('/customer/wallet/transactions', {
    params: { limit }
  })
  return response.data.data
}

export async function getCustomerWalletTransactionsPage(params: WalletTransactionQuery) {
  const response = await http.get<ApiResponse<PageResult<WalletTransaction>>>('/customer/wallet/transactions', {
    params: cleanWalletQuery(params)
  })
  return response.data.data
}

export async function getAdminWalletTransactions(params: WalletTransactionQuery) {
  const response = await http.get<ApiResponse<WalletTransaction[]>>('/admin/wallet/transactions', {
    params: {
      customerId: params.customerId || undefined,
      transactionType: params.transactionType || undefined,
      limit: params.limit || 50
    }
  })
  return response.data.data
}

export async function getAdminWalletTransactionsPage(params: WalletTransactionQuery) {
  const response = await http.get<ApiResponse<PageResult<WalletTransaction>>>('/admin/wallet/transactions', {
    params: cleanWalletQuery(params)
  })
  return response.data.data
}

export async function rechargeCustomerWallet(payload: AdminRechargePayload) {
  const response = await http.post<ApiResponse<WalletTransaction>>('/admin/wallet/recharge', payload)
  return response.data.data
}

export async function adjustCustomerWallet(payload: AdminBalanceAdjustmentPayload) {
  const response = await http.post<ApiResponse<WalletTransaction>>('/admin/wallet/adjustments', payload)
  return response.data.data
}

export async function getCustomerWalletTransactionTypeConfigs() {
  const response = await http.get<ApiResponse<WalletTransactionTypeConfig[]>>('/customer/wallet/transaction-type-configs')
  return response.data.data
}

export async function getAdminWalletTransactionTypeConfigs() {
  const response = await http.get<ApiResponse<WalletTransactionTypeConfig[]>>('/admin/wallet/transaction-type-configs')
  return response.data.data
}

export async function updateAdminWalletTransactionTypeConfigs(payload: UpdateWalletTransactionTypeConfigPayload) {
  const response = await http.put<ApiResponse<WalletTransactionTypeConfig[]>>('/admin/wallet/transaction-type-configs', payload)
  return response.data.data
}

function cleanWalletQuery(params: WalletTransactionQuery) {
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== '')
  )
}
