function availableStores(): Storage[] {
  if (typeof window === 'undefined') return []
  const stores: Storage[] = []
  try {
    stores.push(window.localStorage)
  } catch {
    // Safari privacy settings can make the storage getter itself throw.
  }
  try {
    stores.push(window.sessionStorage)
  } catch {
    // Keep the application usable for the current in-memory session.
  }
  return stores
}

export function readBrowserStorage(key: string): string | null {
  for (const store of availableStores()) {
    try {
      const value = store.getItem(key)
      if (value !== null) return value
    } catch {
      // Try the next storage implementation.
    }
  }
  return null
}

export function writeBrowserStorage(key: string, value: string): boolean {
  const stores = availableStores()
  for (let index = 0; index < stores.length; index += 1) {
    try {
      stores[index].setItem(key, value)
      for (let staleIndex = index + 1; staleIndex < stores.length; staleIndex += 1) {
        try {
          stores[staleIndex].removeItem(key)
        } catch {
          // A stale fallback is harmless when a higher-priority store is readable.
        }
      }
      return true
    } catch {
      // localStorage may be blocked or full; sessionStorage is the fallback.
    }
  }
  return false
}

export function removeBrowserStorage(key: string) {
  for (const store of availableStores()) {
    try {
      store.removeItem(key)
    } catch {
      // Removal is best-effort when browser storage is restricted.
    }
  }
}
