import { parse, stringify } from 'yaml'

export interface ExecConfig {
  version: string
  difficulties: string[]
  areas?: string[]
  languages: string[]
  parameters: Array<{ name: string, enabled: boolean }>
  criteria: Array<{ name: string, enabled: boolean }>
  llms: string[]
}

export function parseYaml(content: string): ExecConfig {
  return parse(content)
}

export function stringifyYaml(config: Partial<ExecConfig>, originalContent: string): string {
  const originalDoc = parse(originalContent)
  const merged = { ...originalDoc, ...config }
  return stringify(merged, { commentString: '#' })
}

export function updateYamlField(
  content: string,
  field: keyof ExecConfig,
  value: any
): string {
  const doc = parse(content)
  doc[field] = value
  return stringify(doc, { commentString: '#' })
}
