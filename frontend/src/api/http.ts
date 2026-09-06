import axios from 'axios'
import type { AxiosError } from 'axios'
import { readBrowserStorage, removeBrowserStorage } from '@/utils/browserStorage'

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

const AUTH_STORAGE_KEY = 'youou_aso_auth'

interface StoredAuthSession {
  token: string
}

export const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const stored = readBrowserStorage(AUTH_STORAGE_KEY)
  if (stored) {
    try {
      const session = JSON.parse(stored) as StoredAuthSession
      if (session.token) {
        config.headers.Authorization = `Bearer ${session.token}`
      }
    } catch {
      removeBrowserStorage(AUTH_STORAGE_KEY)
    }
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<unknown>>) => {
    const status = error.response?.status
    const data = error.response?.data
    const isBusinessForbidden = Boolean(data && typeof data === 'object' && data.code)
    const isAuthExpired = status === 401 || (status === 403 && !isBusinessForbidden)

    if (isAuthExpired) {
      removeBrowserStorage(AUTH_STORAGE_KEY)
      const currentPath = `${window.location.pathname}${window.location.search}`
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`
      }
    }

    return Promise.reject(error)
  }
)
