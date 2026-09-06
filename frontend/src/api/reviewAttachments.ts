import { http, type ApiResponse } from './http'

export interface ReviewAttachment {
  id: string
  fileName: string
  fileSize: number
  regionCode: string | null
}

const path = (admin: boolean) => `/${admin ? 'admin' : 'customer'}/review-attachments`

export async function listReviewAttachments(orderId: number, admin = false) {
  const response = await http.get<ApiResponse<ReviewAttachment[]>>(path(admin), { params: { orderId } })
  return response.data.data
}

export async function uploadReviewAttachment(file: File, admin = false, customerId?: number | '') {
  const data = new FormData()
  data.append('file', file)
  const response = await http.post<ApiResponse<ReviewAttachment>>(path(admin), data, { params: { customerId: customerId || undefined }, timeout: 120000 })
  return response.data.data
}

export async function downloadReviewAttachment(file: ReviewAttachment, admin = false, customerId?: number | '') {
  const response = await http.get(`${path(admin)}/${encodeURIComponent(file.id)}`, { params: { customerId: customerId || undefined }, responseType: 'blob', timeout: 120000 })
  const url = URL.createObjectURL(response.data)
  const link = document.createElement('a')
  link.href = url
  link.download = file.fileName
  link.click()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}
