export class CoverageKeywordImportError extends Error {
  readonly row: number

  constructor(row: number) {
    super(`Keyword on row ${row} exceeds 255 characters`)
    this.name = 'CoverageKeywordImportError'
    this.row = row
  }
}

export function parseCoverageKeywordText(text: string): string[] {
  const keywords = new Set<string>()
  text.split(/\r\n|\n|\r/).forEach((line, index) => {
    const keyword = line.trim()
    if (!keyword) return
    if (keyword.length > 255) throw new CoverageKeywordImportError(index + 1)
    keywords.add(keyword)
  })
  return [...keywords]
}
