import { Button, Checkbox, ButtonGroup } from '@blueprintjs/core'
import { useState, useEffect } from 'react'
import { api } from '../services/api'

interface FileListProps {
  directory: string
  onFileSelect: (filename: string) => void
}

export function FileList({ directory, onFileSelect }: FileListProps) {
  const [files, setFiles] = useState<string[]>([])
  const [selected, setSelected] = useState<Set<string>>(new Set())
  const [modifiedFiles, setModifiedFiles] = useState<Set<string>>(new Set())

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
    const newSelected = new Set(selected)

    if (isChecked) {
      newSelected.add(filename)
    } else {
      newSelected.delete(filename)
    }

    setSelected(newSelected)
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
    if (selected.size === 0) return

    if (confirm(`Delete ${selected.size} file(s)?`)) {
      for (const filename of selected) {
        await api.deleteFile(directory, filename)
      }
      setSelected(new Set())
      await loadFiles()
    }
  }

  const handleDownload = () => {
    selected.forEach(filename => {
      if (!modifiedFiles.has(filename)) {
        api.downloadFile(directory, filename)
      }
    })
  }

  return (
    <div className="file-list">
      <ButtonGroup fill>
        <Button icon="document" onClick={handleCreateFile}>Create</Button>
        <Button icon="trash" onClick={handleDelete}>Delete</Button>
        <Button icon="download" onClick={handleDownload}>Download</Button>
        <Button icon="refresh" onClick={loadFiles}>Reload</Button>
      </ButtonGroup>

      <div className="files">
        {files.map(filename => (
          <div key={filename} className="file-item">
            <Checkbox
              checked={selected.has(filename)}
              onChange={(e) => handleCheckboxChange(filename, e)}
            />
            <span
              className={modifiedFiles.has(filename) ? 'modified' : ''}
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
