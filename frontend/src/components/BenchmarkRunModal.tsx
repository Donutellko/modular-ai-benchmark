import { useState, useEffect } from 'react'
import { Dialog, ProgressBar, Intent, Button } from '@blueprintjs/core'
import { api } from '../services/api'

interface BenchmarkRunModalProps {
  isOpen: boolean
  onClose: () => void
  configFile: string
  onComplete: (resultFile: string) => void
}

interface TaskProgress {
  total: number
  completed: number
  filtered: number
  error: number
  inProgress: number
}

export function BenchmarkRunModal({ isOpen, onClose, configFile, onComplete }: BenchmarkRunModalProps) {
  const [selectedTasks, setSelectedTasks] = useState<Set<string>>(new Set())
  const [resultFile, setResultFile] = useState('')
  const [runId, setRunId] = useState<string | null>(null)
  const [progress, setProgress] = useState<Record<string, TaskProgress>>({})

  useEffect(() => {
    setResultFile(`${configFile.replace('.yaml', '')}_${new Date().toISOString().replace(/[:.]/g, '-')}`)
  }, [configFile])

  useEffect(() => {
    let interval: number
    if (runId) {
      interval = window.setInterval(async () => {
        const status = await api.getBenchmarkStatus(runId)
        setProgress(status.progress)
        if (status.status === 'completed') {
          clearInterval(interval)
          onComplete(runId)
          onClose()
        }
      }, 2000)
    }
    return () => clearInterval(interval)
  }, [runId])

  const handleStart = async () => {
    if (selectedTasks.size === 0) return
    const id = await api.runBenchmark(configFile, Array.from(selectedTasks), resultFile)
    setRunId(id)
  }

  const renderProgressBar = (taskName: string, progress: TaskProgress) => {
    const total = progress.total - progress.filtered
    const percent = total > 0 ? (progress.completed + progress.error) / total * 100 : 0

    return (
      <div key={taskName} className="progress-item">
        <div className="task-name">{taskName}</div>
        <div className="progress-section">
          <ProgressBar
            value={percent / 100}
            intent={Intent.PRIMARY}
            className="stacked-bar"
          />
          <span className="progress-label">
            ({progress.completed + progress.error}/{total})
          </span>
        </div>
      </div>
    )
  }

  return (
    <Dialog
      isOpen={isOpen}
      onClose={onClose}
      title="Run Benchmark"
      className="benchmark-modal"
    >
      {!runId ? (
        <div className="setup-section">
          <div className="task-list">
            {/* TODO: Load and display task sources */}
          </div>
          <input
            value={resultFile}
            onChange={e => setResultFile(e.target.value)}
            placeholder="Result filename"
          />
          <Button onClick={handleStart} disabled={selectedTasks.size === 0}>
            Start Benchmark
          </Button>
        </div>
      ) : (
        <div className="progress-section">
          {Object.entries(progress).map(([task, prog]) =>
            renderProgressBar(task, prog)
          )}
        </div>
      )}
    </Dialog>
  )
}
