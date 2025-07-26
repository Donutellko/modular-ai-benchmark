import { useState, useRef, useCallback } from 'react'
import { Button } from '@blueprintjs/core'
import { FileList } from './components/FileList'
import { YamlEditor } from './components/YamlEditor'
import { ModifiedFilesProvider } from './context/ModifiedFilesContext'
import '@blueprintjs/core/lib/css/blueprint.css'
import './App.css'

type Section = 'exec_configs' | 'task_sources' | 'bench_results'

export default function App() {
  const [isCollapsed, setCollapsed] = useState(false)
  const [activeSection, setActiveSection] = useState<Section>('exec_configs')
  const [selectedFile, setSelectedFile] = useState<string | null>(null)
  const [modifiedFiles, setModifiedFiles] = useState<Set<string>>(new Set())
  const leftPanelRef = useRef<HTMLDivElement>(null)
  const [isResizing, setIsResizing] = useState(false)

  const handleFileModified = (filename: string, isModified: boolean) => {
    setModifiedFiles(prev => {
      const next = new Set(prev)
      if (isModified) {
        next.add(filename)
      } else {
        next.delete(filename)
      }
      return next
    })
  }

  const startResizing = useCallback((e: React.MouseEvent) => {
    if (isCollapsed) return
    window.addEventListener('mousemove', handleMouseMove)
    window.addEventListener('mouseup', stopResizing)
    setIsResizing(true)
    e.preventDefault()
  }, [isCollapsed])

  const stopResizing = useCallback(() => {
    window.removeEventListener('mousemove', handleMouseMove)
    window.removeEventListener('mouseup', stopResizing)
    setIsResizing(false)
  }, [])

  const handleMouseMove = useCallback((e: MouseEvent) => {
    if (!leftPanelRef.current) return
    const newWidth = e.clientX
    if (newWidth >= 200 && newWidth <= window.innerWidth * 0.5) {
      leftPanelRef.current.style.width = `${newWidth}px`
    }
  }, [])

  return (
    <ModifiedFilesProvider>
      <div className="app-container">
        <div
          ref={leftPanelRef}
          className={`left-panel ${isCollapsed ? 'collapsed' : ''} ${isResizing ? 'resizing' : ''}`}
        >
          <div className="resize-handle" onMouseDown={startResizing}></div>
          <div className="left-panel-content">
            <Button
              icon={isCollapsed ? 'chevron-right' : 'chevron-left'}
              minimal
              className="collapse-button"
              onClick={() => setCollapsed(!isCollapsed)}
            />

            {isCollapsed ? (
              <div className="vertical-text-container">
                <div
                  className="vertical-text"
                  onClick={() => {
                    setActiveSection('exec_configs')
                    setCollapsed(false)
                  }}
                >
                  Execution Configurations
                </div>
                <div
                  className="vertical-text"
                  onClick={() => {
                    setActiveSection('task_sources')
                    setCollapsed(false)
                  }}
                >
                  Task Sources
                </div>
                <div
                  className="vertical-text"
                  onClick={() => {
                    setActiveSection('bench_results')
                    setCollapsed(false)
                  }}
                >
                  Benchmark Runs
                </div>
              </div>
            ) : (
              <div>
                <div
                  className="section-header"
                  onClick={() => setActiveSection('exec_configs')}
                >
                  Execution Configurations
                </div>
                <div className={`section-content ${activeSection === 'exec_configs' ? 'expanded' : ''}`}>
                  <FileList
                    directory="exec_configs"
                    onFileSelect={setSelectedFile}
                    selectedFile={selectedFile}
                    modifiedFiles={modifiedFiles}
                  />
                </div>

                <div
                  className="section-header"
                  onClick={() => setActiveSection('task_sources')}
                >
                  Task Sources
                </div>
                <div className={`section-content ${activeSection === 'task_sources' ? 'expanded' : ''}`}>
                  <FileList
                    directory="task_sources"
                    onFileSelect={setSelectedFile}
                    selectedFile={selectedFile}
                    modifiedFiles={modifiedFiles}
                  />
                </div>

                <div
                  className="section-header"
                  onClick={() => setActiveSection('bench_results')}
                >
                  Benchmark Runs
                </div>
                <div className={`section-content ${activeSection === 'bench_results' ? 'expanded' : ''}`}>
                  <FileList
                    directory="bench_results"
                    onFileSelect={setSelectedFile}
                    selectedFile={selectedFile}
                    modifiedFiles={modifiedFiles}
                  />
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="right-panel">
          <YamlEditor
            directory={activeSection}
            filename={selectedFile}
            onModified={handleFileModified}
            isModified={selectedFile ? modifiedFiles.has(selectedFile) : false}
          />
        </div>
      </div>
    </ModifiedFilesProvider>
  )
}
