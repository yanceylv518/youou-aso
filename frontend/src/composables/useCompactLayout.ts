import { onBeforeUnmount, ref } from 'vue'

export function useCompactLayout() {
  const query = window.matchMedia('(max-width: 700px)')
  const compact = ref(query.matches)
  const update = () => { compact.value = query.matches }
  query.addEventListener('change', update)
  onBeforeUnmount(() => query.removeEventListener('change', update))
  return compact
}
