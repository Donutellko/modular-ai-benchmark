import { useState, useEffect } from 'react'
import { Button, ButtonGroup, Toaster, Position, Intent } from '@blueprintjs/core'
import Editor from '@monaco-editor/react'
import { ExecConfigForm } from './ExecConfigForm'
import { TaskSourceEditor } from './TaskSourceEditor'
import { RunDialog } from './RunDialog'
import { StatusDialog } from './StatusDialog'
import { useModifiedFiles } from '../context/ModifiedFilesContext'
import { api } from '../services/api'

const toaster = Toaster.create({
  position: Position.TOP,
});

interface YamlEditorProps {
  directory: string
  filename: string | null
  onModified: (filename: string, isModified: boolean) => void
  isModified: boolean
}

export function YamlEditor({ directory, filename, onModified, isModified }: YamlEditorProps) {
  const [content, setContent] = useState('')
  const [originalContent, setOriginalContent] = useState('')
  const [isFormView, setIsFormView] = useState(true)
  const [showRunDialog, setShowRunDialog] = useState(false);
  const [showStatusDialog, setShowStatusDialog] = useState(false);
  const [statusFile, setStatusFile] = useState<string>('');
  const { getModifiedContent, setModifiedContent, clearModifiedContent } = useModifiedFiles()

  useEffect(() => {
    if (filename) {
      const modifiedContent = getModifiedContent(directory, filename)
      if (modifiedContent !== null) {
        setContent(modifiedContent)
      } else {
        loadFile()
      }
    } else {
      setContent('')
      setOriginalContent('')
    }
  }, [directory, filename])

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
    clearModifiedContent(directory, filename)
    onModified(filename, false)
  }

  const handleContentChange = (value: string = '') => {
    setContent(value)
    if (filename) {
      setModifiedContent(directory, filename, value)
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

  const handleRunClick = () => {
    if (isModified) {
      toaster.show({
        message: "Please save your changes before running the benchmark",
        intent: Intent.WARNING
      });
      return;
    }
    setShowRunDialog(true);
  };

  const handleRunStarted = (statusFilePath: string) => {
    setStatusFile(statusFilePath);
    setShowRunDialog(false);
    setShowStatusDialog(true);
  };

  const editorHeight = '100px' // Height for 5 lines approximately
  const isTaskSource = directory === 'task_sources'

  if (!filename) {
    return (
      <div className="empty-editor-state">
        Select a file to edit
      </div>
    );
  }

  return (
    <div className="yaml-editor">
      <ButtonGroup>
        <Button
          icon={isFormView ? "code" : "form"}
          text={isFormView ? "View as YAML" : "View as Form"}
          onClick={() => setIsFormView(!isFormView)}
        />
        <Button
          icon="floppy-disk"
          text="Save"
          onClick={handleSave}
          disabled={!isModified}
        />
        <Button
          icon="history"
          text="History"
          onClick={() => {/* TODO */}}
        />
        {directory === 'exec_configs' && (
          <Button
            icon="play"
            intent="success"
            text="Run"
            onClick={handleRunClick}
          />
        )}
      </ButtonGroup>

      {isFormView ? (
        isTaskSource ? (
          <TaskSourceEditor
            content={content}
            onContentChange={handleContentChange}
          />
        ) : (
          <ExecConfigForm
            content={content}
            onContentChange={handleContentChange} // Changed from onChange to onContentChange
          />
        )
      ) : (
        <Editor
          height="90vh"
          defaultLanguage="yaml"
          value={content}
          onChange={(value) => {
            if (!value) return
            handleContentChange(value)
          }}
          options={{
            minimap: { enabled: false }
          }}
        />
      )}

      {directory === 'exec_configs' && (
        <>
          <RunDialog
            isOpen={showRunDialog}
            onClose={() => setShowRunDialog(false)}
            execConfigFile={filename}
            onRunStarted={handleRunStarted}
          />
          <StatusDialog
            isOpen={showStatusDialog}
            onClose={() => setShowStatusDialog(false)}
            statusFile={statusFile}
          />
        </>
      )}
    </div>
  );
}
