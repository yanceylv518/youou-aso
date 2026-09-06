type MessageTree = Record<string, unknown>

function flatten(value: unknown, prefix = '', output = new Map<string, unknown>()) {
  if (value && typeof value === 'object' && !Array.isArray(value)) {
    for (const [key, child] of Object.entries(value as MessageTree)) {
      flatten(child, prefix ? `${prefix}.${key}` : key, output)
    }
  } else {
    output.set(prefix, value)
  }
  return output
}

function interpolationTokens(value: string) {
  return [...value.matchAll(/\{[^}]+\}/g)].map(([token]) => token).sort()
}

export function validateLocaleMessages(messages: Record<string, MessageTree>, referenceLocale = 'en-US') {
  const reference = flatten(messages[referenceLocale])
  const errors: string[] = []
  for (const [locale, tree] of Object.entries(messages)) {
    const current = flatten(tree)
    for (const key of reference.keys()) if (!current.has(key)) errors.push(`${locale}: missing ${key}`)
    for (const key of current.keys()) if (!reference.has(key)) errors.push(`${locale}: unexpected ${key}`)
    for (const [key, value] of current) if (typeof value === 'string' && !value.trim()) errors.push(`${locale}: empty ${key}`)
    for (const [key, referenceValue] of reference) {
      const currentValue = current.get(key)
      if (typeof referenceValue !== 'string' || typeof currentValue !== 'string') continue
      if (interpolationTokens(referenceValue).join('|') !== interpolationTokens(currentValue).join('|')) {
        errors.push(`${locale}: interpolation mismatch ${key}`)
      }
    }
  }
  if (errors.length) throw new Error(`Invalid locale messages:\n${errors.slice(0, 100).join('\n')}`)
}
