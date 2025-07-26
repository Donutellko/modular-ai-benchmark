import { useState, useEffect } from 'react'
import { Button, ButtonGroup } from '@blueprintjs/core'
import Editor from '@monaco-editor/react'
import { api } from '../services/api'

interface YamlEditorProps {
  directory: string
  filename: string | null
  onModified: (filename: string, isModified: boolean) => void
}

export function YamlEditor({ directory, filename, onModified }: YamlEditorProps) {
  const [content, setContent] = useState('')
  const [isEditing, setIsEditing] = useState(false)
  const [originalContent, setOriginalContent] = useState('')

  useEffect(() => {
    if (filename) {
      loadFile()
    } else {
      setContent('')
      setOriginalContent('')
    }
  }, [filename])

  const loadFile = async () => {
    if (!filename) return
    const content = await api.getFile(directory, filename)
    setContent(content)
    setOriginalContent(content)
    setIsEditing(false)
  }

  const handleSave = async () => {
    if (!filename) return
    await api.updateFile(directory, filename, content)
    setOriginalContent(content)
    onModified(filename, false)
  }

  const handleContentChange = (value: string = '') => {
    setContent(value)
    if (filename) {
      onModified(filename, value !== originalContent)
    }
  }

  const handleDuplicate = async () => {
    if (!filename) return
    const newName = prompt('Enter new file name:')
    if (newName) {
      await api.createFile(directory, newName)
      await api.updateFile(directory, newName, content)
    }
  }

  if (!filename) {
    return <div className="no-file">No file selected</div>
  }

  return (
    <div className="yaml-editor">
      <ButtonGroup>
        <Button icon="floppy-disk" onClick={handleSave}>Save</Button>
        <Button icon="duplicate" onClick={handleDuplicate}>Duplicate</Button>
        <Button icon="history">History</Button>
        {directory === 'exec_configs' && (
          <Button icon="play" intent="success">Run Benchmark</Button>
        )}
      </ButtonGroup>

      <Editor
        height="90vh"
        defaultLanguage="yaml"
        value={content}
        onChange={handleContentChange}
        options={{
          minimap: { enabled: false },
          lineNumbers: 'on',
          scrollBeyondLastLine: false,
          wordWrap: 'on'
        }}
      />
    </div>
  )
}
