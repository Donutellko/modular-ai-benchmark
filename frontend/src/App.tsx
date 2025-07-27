import { useState, useRef, useCallback } from 'react'
import { Button, Collapse } from '@blueprintjs/core'
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
  const [expandedSection, setExpandedSection] = useState<Section>('exec_configs');
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

  const switchSection = (section: Section) => {
    setActiveSection(section);
    setExpandedSection(section);
    setSelectedFile(null);
    setCollapsed(false);
  };

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
                    switchSection('exec_configs')
                    setCollapsed(false)
                  }}
                >
                  Execution Configurations
                </div>
                <div
                  className="vertical-text"
                  onClick={() => {
                    switchSection('task_sources')
                    setCollapsed(false)
                  }}
                >
                  Task Sources
                </div>
                <div
                  className="vertical-text"
                  onClick={() => {
                    switchSection('bench_results')
                    setCollapsed(false)
                  }}
                >
                  Benchmark Runs
                </div>
              </div>
            ) : (
              <div className="sections-container">
                <div className="section">
                  <Button
                    fill
                    large
                    minimal
                    alignText="left"
                    rightIcon={expandedSection === 'exec_configs' ? 'chevron-down' : 'chevron-right'}
                    active={activeSection === 'exec_configs'}
                    onClick={() => switchSection('exec_configs')}
                  >
                    Execution Configurations
                  </Button>
                  <Collapse isOpen={expandedSection === 'exec_configs'}>
                    <div className="section-content">
                      <FileList
                        directory="exec_configs"
                        onFileSelect={setSelectedFile}
                        selectedFile={selectedFile}
                        modifiedFiles={modifiedFiles}
                      />
                    </div>
                  </Collapse>
                </div>

                <div className="section">
                  <Button
                    fill
                    large
                    minimal
                    alignText="left"
                    rightIcon={expandedSection === 'task_sources' ? 'chevron-down' : 'chevron-right'}
                    active={activeSection === 'task_sources'}
                    onClick={() => switchSection('task_sources')}
                  >
                    Task Sources
                  </Button>
                  <Collapse isOpen={expandedSection === 'task_sources'}>
                    <div className="section-content">
                      <FileList
                        directory="task_sources"
                        onFileSelect={setSelectedFile}
                        selectedFile={selectedFile}
                        modifiedFiles={modifiedFiles}
                      />
                    </div>
                  </Collapse>
                </div>

                <div className="section">
                  <Button
                    fill
                    large
                    minimal
                    alignText="left"
                    rightIcon={expandedSection === 'bench_results' ? 'chevron-down' : 'chevron-right'}
                    active={activeSection === 'bench_results'}
                    onClick={() => switchSection('bench_results')}
                  >
                    Benchmark Runs
                  </Button>
                  <Collapse isOpen={expandedSection === 'bench_results'}>
                    <div className="section-content">
                      <FileList
                        directory="bench_results"
                        onFileSelect={setSelectedFile}
                        selectedFile={selectedFile}
                        modifiedFiles={modifiedFiles}
                      />
                    </div>
                  </Collapse>
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
