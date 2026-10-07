import { defineStore } from 'pinia'
import {
  getCurrentAccount,
  login,
  registerCustomer,
  type CurrentAccountResult,
  type LoginPayload,
  type LoginResult,
  type RegisterPayload
} from '@/api/auth'
import { isAxiosError } from 'axios'
import { readAuthSession, clearAuthSession, writeAuthSession } from '@/utils/authStorage'

export type AccountType = 'CUSTOMER' | 'ADMIN'
export type AdminRole = 'ADMIN' | 'SUPER_ADMIN'

export { AUTH_STORAGE_KEY } from '@/utils/authStorage'

export interface StoredAuthSession {
  token: string
  accountId: number | null
  username: string
  email: string
  accountType: AccountType | ''
  roleCode: AdminRole | ''
  forcePasswordChange: boolean
  locale: string
  roleKeys: string[]
  menuCodes: string[]
  permissions: string[]
}

function toStoredSession(state: StoredAuthSession): StoredAuthSession {
  return {
    token: state.token,
    accountId: state.accountId,
    username: state.username,
    email: state.email,
    accountType: state.accountType,
    roleCode: state.roleCode,
    forcePasswordChange: state.forcePasswordChange,
    locale: state.locale,
    roleKeys: state.roleKeys,
    menuCodes: state.menuCodes,
    permissions: state.permissions
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '',
    accountId: null as number | null,
    username: '',
    email: '',
    accountType: '' as AccountType | '',
    roleCode: '' as AdminRole | '',
    forcePasswordChange: false,
    locale: 'en-US',
    roleKeys: [] as string[],
    menuCodes: [] as string[],
    permissions: [] as string[],
    bootstrapped: false
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    isCustomer: (state) => state.accountType === 'CUSTOMER',
    isAdmin: (state) => state.accountType === 'ADMIN',
    isSuperAdmin: (state) => state.roleCode === 'SUPER_ADMIN',
    hasMenu: (state) => (code?: string) => {
      return !code || state.roleCode === 'SUPER_ADMIN' || state.menuCodes.includes(code)
    },
    hasPermission: (state) => (code?: string) => {
      return !code || state.roleCode === 'SUPER_ADMIN' || state.permissions.includes(code)
    }
  },
  actions: {
    restore() {
      const stored = readAuthSession()
      if (!stored) {
        return
      }
      try {
        const session = JSON.parse(stored) as StoredAuthSession
        this.token = session.token || ''
        this.accountId = session.accountId ?? null
        this.username = session.username || ''
        this.email = session.email || ''
        this.accountType = session.accountType || ''
        this.roleCode = session.roleCode || ''
        this.forcePasswordChange = Boolean(session.forcePasswordChange)
        this.locale = session.locale || 'en-US'
        this.roleKeys = Array.isArray(session.roleKeys) ? session.roleKeys : []
        this.menuCodes = Array.isArray(session.menuCodes) ? session.menuCodes : []
        this.permissions = Array.isArray(session.permissions) ? session.permissions : []
      } catch {
        clearAuthSession()
      }
    },
    persist() {
      writeAuthSession(JSON.stringify(toStoredSession(this)))
    },
    applyLoginResult(result: LoginResult) {
      this.token = result.token
      this.accountId = result.accountId
      this.username = result.username
      this.email = result.email
      this.accountType = result.accountType
      this.roleCode = result.roleCode
      this.forcePasswordChange = result.forcePasswordChange
      this.locale = result.preferredLocale || this.locale
      this.roleKeys = result.roleKeys || []
      this.menuCodes = result.menuCodes || []
      this.permissions = result.permissions || []
      this.persist()
    },
    applyCurrentAccount(result: CurrentAccountResult) {
      this.accountId = result.accountId
      this.username = result.username
      this.email = result.email
      this.accountType = result.accountType
      this.roleCode = result.roleCode
      this.forcePasswordChange = result.forcePasswordChange
      this.locale = result.preferredLocale || this.locale
      this.roleKeys = result.roleKeys || []
      this.menuCodes = result.menuCodes || []
      this.permissions = result.permissions || []
      this.persist()
    },
    async bootstrap() {
      this.restore()
      if (!this.token) {
        this.bootstrapped = true
        return
      }
      try {
        const account = await getCurrentAccount()
        this.applyCurrentAccount(account)
      } catch (error) {
        if (isAuthenticationFailure(error)) {
          this.logout()
        }
      } finally {
        this.bootstrapped = true
      }
    },
    async login(payload: LoginPayload) {
      const result = await login(payload)
      this.applyLoginResult(result)
      return result
    },
    async register(payload: RegisterPayload) {
      return registerCustomer(payload)
    },
    logout() {
      this.token = ''
      this.accountId = null
      this.username = ''
      this.email = ''
      this.accountType = ''
      this.roleCode = ''
      this.forcePasswordChange = false
      this.roleKeys = []
      this.menuCodes = []
      this.permissions = []
      clearAuthSession()
    }
  }
})

function isAuthenticationFailure(error: unknown) {
  if (!isAxiosError(error)) return false
  const status = error.response?.status
  const data = error.response?.data as { code?: string } | undefined
  return status === 401 || (status === 403 && !data?.code)
}
