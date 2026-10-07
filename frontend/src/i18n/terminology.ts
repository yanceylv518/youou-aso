import terms from './terminology.json'

// Shared business terminology takes precedence over legacy generated locale fragments.
export function applyTerminology(messages: Record<string, object>) {
  for (const [locale, entries] of Object.entries(terms)) {
    for (const [path, value] of Object.entries(entries)) {
      const parts = path.split('.')
      let target = messages[locale] as Record<string, any>
      for (const part of parts.slice(0, -1)) target = target[part]
      target[parts.at(-1)!] = value
    }
  }
}
