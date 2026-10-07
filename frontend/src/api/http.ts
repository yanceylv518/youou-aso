import axios from 'axios'
import type { AxiosError } from 'axios'
import { readAuthToken, clearAuthSession } from '@/utils/authStorage'

export interface ApiResponse<T> {
  success: boolean
  code: string
  message: string
  data: T
}

export interface PageResult<T> {
  items: T[]
  page: number
  pageSize: number
  total: number
}

export const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = readAuthToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<unknown>>) => {
    const status = error.response?.status
    const data = error.response?.data
    const isBusinessForbidden = Boolean(data && typeof data === 'object' && data.code)
    const isAuthExpired = status === 401 || (status === 403 && !isBusinessForbidden)

    const token = readAuthToken()
    const belongsToCurrentSession = token
      ? error.config?.headers?.Authorization === `Bearer ${token}`
      : !error.config?.headers?.Authorization
    if (isAuthExpired && belongsToCurrentSession) {
      clearAuthSession()
      const currentPath = `${window.location.pathname}${window.location.search}`
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`
      }
    }

    return Promise.reject(error)
  }
)
