import { useState, useEffect } from 'react';
import { Dialog, Classes, ProgressBar, Intent } from '@blueprintjs/core';
import { api } from '../services/api';

interface Status {
  task_source: string;
  total: number;
  completed: number;
  in_progress: number;
  filtered_out: number;
  error: number;
}

interface StatusDialogProps {
  isOpen: boolean;
  onClose: () => void;
  statusFile: string;
}

export function StatusDialog({ isOpen, onClose, statusFile }: StatusDialogProps) {
  const [status, setStatus] = useState<Status[]>([]);
  const [isCompleted, setIsCompleted] = useState(false);

  useEffect(() => {
    if (isOpen && !isCompleted) {
      const interval = setInterval(async () => {
        try {
          const content = await api.getFile('bench_status', statusFile);
          const status = JSON.parse(content);
          setStatus(status.task_sources || []);

          // Check if all tasks are completed or errored
          const allDone = status.task_sources?.every(
            (s: Status) => s.completed + s.error + s.filtered_out === s.total
          );
          if (allDone) {
            setIsCompleted(true);
            clearInterval(interval);
          }
        } catch (e) {
          console.error('Failed to fetch status:', e);
        }
      }, 2000);

      return () => clearInterval(interval);
    }
  }, [isOpen, statusFile, isCompleted]);

  const renderProgressBar = (status: Status) => {
    const total = status.total - status.filtered_out;
    if (total === 0) return null;

    const segments = [
      {
        ratio: status.completed / total,
        intent: Intent.SUCCESS,
      },
      {
        ratio: status.in_progress / total,
        intent: Intent.PRIMARY,
      },
      {
        ratio: status.error / total,
        intent: Intent.DANGER,
      },
    ].filter(s => s.ratio > 0);

    return (
      <div className="status-row" key={status.task_source}>
        <div className="status-label">
          {status.task_source}
          <span className="task-count">
            ({status.completed + status.error}/{total})
          </span>
        </div>
        <div className="progress-container">
          <ProgressBar
            animate={!isCompleted}
            stripes={!isCompleted}
            intent={segments[0]?.intent}
            value={1}
            className="filtered-progress"
          />
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
          {status.map(renderProgressBar)}
        </div>
      </div>
    </Dialog>
  );
}
