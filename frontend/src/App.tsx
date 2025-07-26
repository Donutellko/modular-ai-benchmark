import { useState } from 'react'
import { Button, Collapse } from '@blueprintjs/core'
import { FileList } from './components/FileList'
import { YamlEditor } from './components/YamlEditor'
import '@blueprintjs/core/lib/css/blueprint.css'
import './App.css'

type Section = 'exec_configs' | 'task_sources' | 'bench_runs'

export function App() {
  const [isCollapsed, setCollapsed] = useState(false)
  const [activeSection, setActiveSection] = useState<Section>('exec_configs')
  const [selectedFile, setSelectedFile] = useState<string | null>(null)
  const [modifiedFiles, setModifiedFiles] = useState<Set<string>>(new Set())

  const handleFileModified = (filename: string, isModified: boolean) => {
    const newModified = new Set(modifiedFiles)
    if (isModified) {
      newModified.add(filename)
    } else {
      newModified.delete(filename)
    }
    setModifiedFiles(newModified)
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
            <div className="vertical-text" onClick={() => {
              setActiveSection('exec_configs')
              setCollapsed(false)
            }}>Execution Configurations</div>
            <div className="vertical-text" onClick={() => {
              setActiveSection('task_sources')
              setCollapsed(false)
            }}>Task Sources</div>
          </div>
        ) : (
          <>
            <Collapse
              isOpen={activeSection === 'exec_configs'}
              title="Execution Configurations"
              onClick={() => setActiveSection('exec_configs')}
            >
              <FileList
                directory="exec_configs"
                onFileSelect={setSelectedFile}
              />
            </Collapse>

            <Collapse
              isOpen={activeSection === 'task_sources'}
              title="Task Sources"
              onClick={() => setActiveSection('task_sources')}
            >
              <FileList
                directory="task_sources"
                onFileSelect={setSelectedFile}
              />
            </Collapse>

            <Collapse
              isOpen={activeSection === 'bench_runs'}
              title="Benchmark Runs"
              onClick={() => setActiveSection('bench_runs')}
            >
              <FileList
                directory="bench_results"
                onFileSelect={setSelectedFile}
              />
            </Collapse>
          </>
        )}
      </div>

      <div className="right-panel">
        <YamlEditor
          directory={activeSection}
          filename={selectedFile}
          onModified={handleFileModified}
        />
      </div>
    </div>
  )
