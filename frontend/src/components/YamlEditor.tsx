import { useState, useEffect } from 'react'
import { Button, ButtonGroup } from '@blueprintjs/core'
import Editor from '@monaco-editor/react'
import { api } from '../services/api'

interface YamlEditorProps {
  directory: string
  filename: string | null
  onModified: (filename: string, isModified: boolean) => void
  isModified: boolean
}

export function YamlEditor({ directory, filename, onModified, isModified }: YamlEditorProps) {
  const [content, setContent] = useState('')
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

  return (
    <div className="yaml-editor">
      {filename && (
        <div className="editor-header">
          <span className="file-name">
            {filename}
            {isModified && <span className="modified-indicator">*</span>}
          </span>
        </div>
      )}
      {filename ? (
        <>
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
        </>
      ) : (
        <div className="no-file">No file selected</div>
      )}
    </div>
  )
}
