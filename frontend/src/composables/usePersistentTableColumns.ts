import { ref, watch, type Ref } from 'vue'
import { readBrowserStorage, writeBrowserStorage } from '@/utils/browserStorage'

export function usePersistentTableColumns(
  storageKey: string,
  availableKeys: string[],
  defaultKeys: string[]
): { visibleColumns: Ref<string[]>; isColumnVisible: (key: string) => boolean } {
  const available = new Set(availableKeys)
  const fallback = defaultKeys.filter((key) => available.has(key))
  let initial = fallback

  try {
    const saved = JSON.parse(readBrowserStorage(storageKey) || 'null')
    if (Array.isArray(saved)) {
      const valid = saved.map(String).filter((key) => available.has(key))
      if (valid.length) initial = valid
    }
  } catch {
    // Invalid historical settings fall back to the current defaults.
  }

  const visibleColumns = ref<string[]>(initial)
  watch(visibleColumns, (value) => {
    const valid = value.filter((key) => available.has(key))
    writeBrowserStorage(storageKey, JSON.stringify(valid.length ? valid : fallback))
  }, { deep: true })

  return {
    visibleColumns,
    isColumnVisible: (key: string) => visibleColumns.value.includes(key)
  }
}
