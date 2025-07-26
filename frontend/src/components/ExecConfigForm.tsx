import { useState, useEffect } from 'react'
import { FormGroup, Switch, TagInput } from '@blueprintjs/core'
import { ExecConfig, parseYaml, updateYamlField } from '../services/yaml'

interface ExecConfigFormProps {
  content: string
  onChange: (content: string) => void
}

// Default config moved to yaml.ts

export function ExecConfigForm({ content, onChange }: ExecConfigFormProps) {
  const [config, setConfig] = useState<ExecConfig | null>(null)

  useEffect(() => {
    try {
      const parsed = parseYaml(content)
      setConfig(parsed)
    } catch (e) {
      console.error('Failed to parse YAML:', e)
    }
  }, [content])

  if (!config) return null

  const handleArrayChange = (field: keyof ExecConfig, values: string[]) => {
    const newContent = updateYamlField(content, field, values)
    onChange(newContent)
  }

  const handleSwitchChange = (
    section: 'parameters' | 'criteria',
    name: string,
    enabled: boolean
  ) => {
    const items = [...config[section]]
    const index = items.findIndex(item => item.name === name)
    if (index !== -1) {
      items[index] = { ...items[index], enabled }
      const newContent = updateYamlField(content, section, items)
      onChange(newContent)
    }
  }

  return (
    <div className="exec-config-form">
      <FormGroup label="Version">
        <TagInput
          values={[config.version]}
          onChange={values => handleArrayChange('version', values)}
          addOnPaste={false}
          fill
        />
      </FormGroup>

      <FormGroup label="Difficulties">
        <TagInput
          values={config.difficulties}
          onChange={values => handleArrayChange('difficulties', values)}
          addOnPaste
          fill
          placeholder="Add difficulty..."
        />
      </FormGroup>

      <FormGroup label="Areas (Domains)">
        <TagInput
          values={config.areas || []}
          onChange={values => handleArrayChange('areas', values)}
          addOnPaste
          fill
          placeholder="Add area..."
        />
      </FormGroup>

      <FormGroup label="Languages">
        <TagInput
          values={config.languages}
          onChange={values => handleArrayChange('languages', values)}
          addOnPaste
          fill
          placeholder="Add language..."
        />
      </FormGroup>

      <FormGroup label="Parameters">
        <div className="switch-group">
          {config.parameters.map(param => (
            <Switch
              key={param.name}
              checked={param.enabled}
              label={param.name}
              onChange={e => handleSwitchChange(
                'parameters',
                param.name,
                (e.target as HTMLInputElement).checked
              )}
            />
          ))}
        </div>
      </FormGroup>

      <FormGroup label="Criteria">
        <div className="switch-group">
          {config.criteria.map(criterion => (
            <Switch
              key={criterion.name}
              checked={criterion.enabled}
              label={criterion.name}
              onChange={e => handleSwitchChange(
                'criteria',
                criterion.name,
                (e.target as HTMLInputElement).checked
              )}
            />
          ))}
        </div>
      </FormGroup>

      <FormGroup label="LLMs">
        <TagInput
          values={config.llms}
          onChange={values => handleArrayChange('llms', values)}
          addOnPaste
          fill
          placeholder="Add LLM..."
        />
      </FormGroup>
    </div>
  )
}
