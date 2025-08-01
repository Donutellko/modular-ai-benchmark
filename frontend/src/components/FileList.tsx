import { Button, Checkbox, ButtonGroup } from '@blueprintjs/core'
import { useState, useEffect } from 'react'
import { api } from '../services/api'
import { useModifiedFiles } from '../context/ModifiedFilesContext'

interface FileListProps {
  directory: string
  onFileSelect: (filename: string) => void
  selectedFile: string | null
  modifiedFiles: Set<string>
}

export function FileList({ directory, onFileSelect, selectedFile, modifiedFiles }: FileListProps) {
  const [files, setFiles] = useState<string[]>([])
  const [selectedForAction, setSelectedForAction] = useState<Set<string>>(new Set())
  const [lastSelected, setLastSelected] = useState<string | null>(null)
  const { getModifiedContent, clearModifiedContent } = useModifiedFiles()

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

  const handleClick = (filename: string, event: React.MouseEvent) => {
    onFileSelect(filename)

    // Handle multi-select only for exec_configs
    if (directory === 'exec_configs') {
      if (event.shiftKey && lastSelected) {
        const startIndex = files.indexOf(lastSelected)
        const endIndex = files.indexOf(filename)
        const filesToSelect = files.slice(
          Math.min(startIndex, endIndex),
          Math.max(startIndex, endIndex) + 1
        )
        setSelectedForAction(new Set([...selectedForAction, ...filesToSelect]))
      } else if (event.ctrlKey || event.metaKey) {
        const newSelected = new Set(selectedForAction)
        if (newSelected.has(filename)) {
          newSelected.delete(filename)
        } else {
          newSelected.add(filename)
        }
        setSelectedForAction(newSelected)
      } else {
        setSelectedForAction(new Set([filename]))
      }
      setLastSelected(filename)
    } else {
      setSelectedForAction(new Set([filename]))
    }
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

  const handleSaveAll = async () => {
    const modifiedInDir = files.filter(f => modifiedFiles.has(f))
    for (const filename of modifiedInDir) {
      const content = getModifiedContent(directory, filename)
      if (content !== null) {
        await api.updateFile(directory, filename, content)
        clearModifiedContent(directory, filename)
      }
    }
    await loadFiles()
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
        <Button
          icon="floppy-disk"
          onClick={handleSaveAll}
          disabled={!files.some(f => modifiedFiles.has(f))}
        >Save All</Button>
        <Button icon="refresh" onClick={loadFiles}>Reload</Button>
      </ButtonGroup>

      <div className="files">
        {files.map(filename => (
          <div
            key={filename}
            className={`file-item ${selectedFile === filename ? 'selected' : ''} ${modifiedFiles.has(filename) ? 'modified' : ''}`}
            onClick={(e) => handleClick(filename, e)}
          >
            <Checkbox
              checked={selectedForAction.has(filename)}
              onChange={() => {}} // Handled by div click
            />
            <span className="file-name">
              {filename}
              {modifiedFiles.has(filename) && '*'}
            </span>
          </div>
        ))}
      </div>
    </div>
  )
}
