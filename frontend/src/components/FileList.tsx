import { Button, Checkbox, ButtonGroup } from '@blueprintjs/core'
import { useState, useEffect } from 'react'
import { api } from '../services/api'

interface FileListProps {
  directory: string
  onFileSelect: (filename: string) => void
  selectedFile: string | null
  modifiedFiles: Set<string>
}

export function FileList({ directory, onFileSelect, selectedFile, modifiedFiles }: FileListProps) {
  const [files, setFiles] = useState<string[]>([])
  const [selectedForAction, setSelectedForAction] = useState<Set<string>>(new Set())

  useEffect(() => {
    loadFiles()
  }, [directory])

  const loadFiles = async () => {
    try {
      const fileList = await api.listFiles(directory)
      setFiles(Array.isArray(fileList) ? fileList : [])
    } catch (error) {
      console.error('Error loading files:', error)
      setFiles([])
    }
  }

  const handleCheckboxChange = (filename: string, event: React.FormEvent<HTMLInputElement>) => {
    const isChecked = (event.target as HTMLInputElement).checked
    setSelectedForAction(prev => {
      const next = new Set(prev)
      if (isChecked) {
        next.add(filename)
      } else {
        next.delete(filename)
      }
      return next
    })
  }

  const handleCreateFile = async () => {
    const filename = prompt('Enter new file name:')
    if (filename) {
      await api.createFile(directory, filename)
      await loadFiles()
      onFileSelect(filename)
    }
  }

  const handleDelete = async () => {
    if (selectedForAction.size === 0) return

    if (confirm(`Delete ${selectedForAction.size} file(s)?`)) {
      for (const filename of selectedForAction) {
        await api.deleteFile(directory, filename)
      }
      setSelectedForAction(new Set())
      await loadFiles()
    }
  }

  const handleDownload = () => {
    selectedForAction.forEach(filename => {
      if (!modifiedFiles.has(filename)) {
        api.downloadFile(directory, filename)
      }
    })
  }

  return (
    <div className="file-list">
      <ButtonGroup fill>
        <Button icon="document" onClick={handleCreateFile}>Create</Button>
        <Button
          icon="trash"
          onClick={handleDelete}
          disabled={selectedForAction.size === 0}
        >Delete</Button>
        <Button
          icon="download"
          onClick={handleDownload}
          disabled={Array.from(selectedForAction).some(f => modifiedFiles.has(f))}
        >Download</Button>
        <Button icon="refresh" onClick={loadFiles}>Reload</Button>
      </ButtonGroup>

      <div className="files">
        {files.map(filename => (
          <div
            key={filename}
            className={`file-item ${selectedFile === filename ? 'selected' : ''} ${modifiedFiles.has(filename) ? 'modified' : ''}`}
          >
            <Checkbox
              checked={selectedForAction.has(filename)}
              onChange={(e) => handleCheckboxChange(filename, e)}
            />
            <span
              className="file-name"
              onClick={() => onFileSelect(filename)}
            >
              {filename}
              {modifiedFiles.has(filename) && '*'}
            </span>
          </div>
        ))}
      </div>
    </div>
  )
}
