import { useState, useRef, useCallback } from 'react'
import { Button, Collapse } from '@blueprintjs/core'
import { FileList } from './components/FileList'
import { YamlEditor } from './components/YamlEditor'
import { BenchmarkResultsView } from './components/BenchmarkResults/BenchmarkResultsView'
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
  }

  return (
    <ModifiedFilesProvider>
      <div className="app">
        <div
          ref={leftPanelRef}
          className={`left-panel ${isCollapsed ? 'collapsed' : ''}`}
          style={{ width: isCollapsed ? '40px' : undefined }}
        >
          <Button
            icon={isCollapsed ? 'chevron-right' : 'chevron-left'}
            minimal
            onClick={() => setCollapsed(!isCollapsed)}
            className="collapse-button"
          />

          {isCollapsed ? (
            <div className="vertical-text-container">
              <div
                className={`vertical-text ${activeSection === 'exec_configs' ? 'active' : ''}`}
                onClick={() => switchSection('exec_configs')}
              >
                Execution Configurations
              </div>
              <div
                className={`vertical-text ${activeSection === 'task_sources' ? 'active' : ''}`}
                onClick={() => switchSection('task_sources')}
              >
                Task Sources
              </div>
              <div
                className={`vertical-text ${activeSection === 'bench_results' ? 'active' : ''}`}
                onClick={() => switchSection('bench_results')}
              >
                Benchmark Results
              </div>
            </div>
          ) : (
            <>
              <div
                className={`section-header ${expandedSection === 'exec_configs' ? 'expanded' : ''}`}
                onClick={() => setExpandedSection('exec_configs')}
              >
                Execution Configurations
                <Collapse isOpen={expandedSection === 'exec_configs'}>
                  <FileList
                    directory="exec_configs"
                    onFileSelect={(file) => {
                      setSelectedFile(file)
                      setActiveSection('exec_configs')
                    }}
                    selectedFile={activeSection === 'exec_configs' ? selectedFile : null}
                    modifiedFiles={modifiedFiles}
                  />
                </Collapse>
              </div>

              <div
                className={`section-header ${expandedSection === 'task_sources' ? 'expanded' : ''}`}
                onClick={() => setExpandedSection('task_sources')}
              >
                Task Sources
                <Collapse isOpen={expandedSection === 'task_sources'}>
                  <FileList
                    directory="task_sources"
                    onFileSelect={(file) => {
                      setSelectedFile(file)
                      setActiveSection('task_sources')
                    }}
                    selectedFile={activeSection === 'task_sources' ? selectedFile : null}
                    modifiedFiles={modifiedFiles}
                  />
                </Collapse>
              </div>

              <div
                className={`section-header ${expandedSection === 'bench_results' ? 'expanded' : ''}`}
                onClick={() => setExpandedSection('bench_results')}
              >
                Benchmark Results
                <Collapse isOpen={expandedSection === 'bench_results'}>
                  <FileList
                    directory="bench_results"
                    onFileSelect={(file) => {
                      setSelectedFile(file)
                      setActiveSection('bench_results')
                    }}
                    selectedFile={activeSection === 'bench_results' ? selectedFile : null}
                    modifiedFiles={new Set()}
                  />
                </Collapse>
              </div>
            </>
          )}
        </div>

        <div className="right-panel">
          {activeSection === 'bench_results' ? (
            <BenchmarkResultsView
              selectedFile={selectedFile}
              onFileSelect={setSelectedFile}
            />
          ) : (
            selectedFile && (
              <YamlEditor
                directory={activeSection}
                filename={selectedFile}
                onModified={handleFileModified}
              />
            )
          )}
        </div>
      </div>
    </ModifiedFilesProvider>
  )
}
