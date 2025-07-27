import { useState, useEffect } from 'react';
import { Dialog, Classes, ProgressBar, Intent } from '@blueprintjs/core';
import { api } from '../services/api';

interface TaskSourceStatus {
  total: number;
  completed: number;
  inProgress: number;
  filteredOut: number;
  error: number;
}

interface Status {
  execConfigFile: string;
  taskSourceFiles: string[];
  resultFilename: string;
  taskSourceStatuses: { [key: string]: TaskSourceStatus };
}

interface StatusDialogProps {
  isOpen: boolean;
  onClose: () => void;
  statusFile: string;
}

export function StatusDialog({ isOpen, onClose, statusFile }: StatusDialogProps) {
  const [status, setStatus] = useState<Status | null>(null);
  const [isCompleted, setIsCompleted] = useState(false);

  useEffect(() => {
    if (isOpen && !isCompleted) {
      const interval = setInterval(async () => {
        try {
          const data = await api.getBenchmarkStatus(statusFile);
          setStatus(data);

          // Check if all tasks are completed
          if (data.taskSourceStatuses) {
            const allDone = Object.values(data.taskSourceStatuses).every(
              (s: TaskSourceStatus) => s.completed + s.error + s.filteredOut === s.total
            );
            if (allDone) {
              setIsCompleted(true);
              clearInterval(interval);
            }
          }
        } catch (e) {
          console.error('Failed to fetch status:', e);
        }
      }, 2000);

      return () => clearInterval(interval);
    }
  }, [isOpen, statusFile, isCompleted]);

  const renderProgressBar = (taskSourceName: string, status: TaskSourceStatus) => {
    const total = status.total - status.filteredOut;
    if (total === 0) return null;

    const segments = [
      {
        ratio: status.completed / total,
        intent: Intent.SUCCESS,
      },
      {
        ratio: status.inProgress / total,
        intent: Intent.PRIMARY,
      },
      {
        ratio: status.error / total,
        intent: Intent.DANGER,
      },
    ].filter(s => s.ratio > 0);

    return (
      <div className="status-row" key={taskSourceName}>
        <div className="status-label">
          {taskSourceName}
          <span className="task-count">
            ({status.completed + status.error}/{total})
          </span>
        </div>
        <div className="progress-container">
          {status.filteredOut > 0 && (
            <ProgressBar
              animate={!isCompleted}
              stripes={!isCompleted}
              intent={Intent.NONE}
              value={1}
              className="filtered-progress"
            />
          )}
          {segments.map((segment, i) => (
            <ProgressBar
              key={i}
              animate={!isCompleted}
              stripes={!isCompleted}
              intent={segment.intent}
              value={segment.ratio}
            />
          ))}
        </div>
      </div>
    );
  };

  return (
    <Dialog
      isOpen={isOpen}
      onClose={onClose}
      title="Benchmark Progress"
      style={{ width: '600px' }}
    >
      <div className={Classes.DIALOG_BODY}>
        <div className="status-container">
          {status?.taskSourceStatuses &&
            Object.entries(status.taskSourceStatuses).map(([name, status]) =>
              renderProgressBar(name, status)
            )}
        </div>
      </div>
    </Dialog>
  );
}
