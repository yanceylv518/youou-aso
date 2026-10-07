// Authentication must never fall back to storage shared by other tabs.
export const AUTH_STORAGE_KEY = 'youou_aso_tab_auth'
let memorySession: string | null = null
let storageUnavailable = false

function clearLegacySession() {
  try {
    window.localStorage.removeItem('youou_aso_auth')
  } catch { /* Shared storage may be unavailable. */ }
  try {
    window.sessionStorage.removeItem('youou_aso_auth')
  } catch { /* Legacy sessions are never read. */ }
}

export function readAuthSession(): string | null {
  clearLegacySession()
  if (!storageUnavailable) {
    try {
      memorySession = window.sessionStorage.getItem(AUTH_STORAGE_KEY)
    } catch {
      storageUnavailable = true
    }
  }
  return memorySession
}

export function writeAuthSession(value: string | null) {
  clearLegacySession()
  memorySession = value
  try {
    if (value === null) window.sessionStorage.removeItem(AUTH_STORAGE_KEY)
    else window.sessionStorage.setItem(AUTH_STORAGE_KEY, value)
    storageUnavailable = false
  } catch {
    // Keep requests and the auth store consistent for this page lifetime.
    storageUnavailable = true
  }
}

export function clearAuthSession() {
  writeAuthSession(null)
}

export function readAuthToken(): string {
  const stored = readAuthSession()
  if (!stored) return ''
  try {
    const session = JSON.parse(stored)
    return typeof session?.token === 'string' ? session.token : ''
  } catch {
    clearAuthSession()
    return ''
  }
}
