import { useState } from 'react'
import { Button } from '@blueprintjs/core'
import { FileList } from './components/FileList'
import { YamlEditor } from './components/YamlEditor'
import '@blueprintjs/core/lib/css/blueprint.css'
import './App.css'

type Section = 'exec_configs' | 'task_sources' | 'bench_results'

export default function App() {
  const [isCollapsed, setCollapsed] = useState(false)
  const [activeSection, setActiveSection] = useState<Section>('exec_configs')
  const [selectedFile, setSelectedFile] = useState<string | null>(null)
  const [modifiedFiles, setModifiedFiles] = useState<Set<string>>(new Set())

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

  return (
    <div className="app-container">
      <div className={`left-panel ${isCollapsed ? 'collapsed' : ''}`}>
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
            {activeSection === 'exec_configs' && (
              <FileList
                directory="exec_configs"
                onFileSelect={setSelectedFile}
                selectedFile={selectedFile}
                modifiedFiles={modifiedFiles}
              />
            )}

            <div
              className="section-header"
              onClick={() => setActiveSection('task_sources')}
            >
              Task Sources
            </div>
            {activeSection === 'task_sources' && (
              <FileList
                directory="task_sources"
                onFileSelect={setSelectedFile}
                selectedFile={selectedFile}
                modifiedFiles={modifiedFiles}
              />
            )}

            <div
              className="section-header"
              onClick={() => setActiveSection('bench_results')}
            >
              Benchmark Runs
            </div>
            {activeSection === 'bench_results' && (
              <FileList
                directory="bench_results"
                onFileSelect={setSelectedFile}
                selectedFile={selectedFile}
                modifiedFiles={modifiedFiles}
              />
            )}
          </div>
        )}
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
  )
}
