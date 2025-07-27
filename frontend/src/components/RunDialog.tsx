import { useState, useEffect } from 'react';
import { Dialog, Classes, Button, Checkbox, FormGroup, InputGroup, Toaster, Position, Intent } from '@blueprintjs/core';
import { api } from '../services/api';

interface RunDialogProps {
  isOpen: boolean;
  onClose: () => void;
  execConfigFile: string;
  onRunStarted: (statusFile: string) => void;
}

const toaster = Toaster.create({
  position: Position.TOP,
});

export function RunDialog({ isOpen, onClose, execConfigFile, onRunStarted }: RunDialogProps) {
  const [taskSources, setTaskSources] = useState<Array<{name: string, selected: boolean}>>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [resultFilename, setResultFilename] = useState('');

  useEffect(() => {
    if (isOpen) {
      loadTaskSources();
      const defaultName = `${execConfigFile.replace('.yaml', '')}_${new Date().toISOString().replace(/[:.]/g, '-')}`;
      setResultFilename(defaultName);
    }
  }, [isOpen, execConfigFile]);

  const loadTaskSources = async () => {
    try {
      const files = await api.listFiles('task_sources');
      setTaskSources(files.map(name => ({ name, selected: false })));
    } catch (e) {
      console.error('Failed to load task sources:', e);
      toaster.show({
        message: 'Failed to load task sources',
        intent: Intent.DANGER,
      });
    }
  };

  const handleRun = async () => {
    const selectedSources = taskSources
      .filter(source => source.selected)
      .map(source => source.name);

    if (selectedSources.length === 0) {
      toaster.show({
        message: 'Please select at least one task source',
        intent: Intent.WARNING,
      });
      return;
    }

    setIsLoading(true);
    try {
      const statusFile = await api.startBenchmark({
        execConfig: execConfigFile,
        taskSources: selectedSources,
        resultFilename: resultFilename + '.yaml'
      });
      onRunStarted(statusFile);
      onClose();
    } catch (e) {
      console.error('Failed to start benchmark:', e);
      toaster.show({
        message: 'Failed to start benchmark',
        intent: Intent.DANGER,
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Dialog
      isOpen={isOpen}
      onClose={onClose}
      title="Run Benchmark"
      style={{ width: '500px' }}
    >
      <div className={Classes.DIALOG_BODY}>
        <FormGroup label="Result filename">
          <InputGroup
            value={resultFilename}
            onChange={(e) => setResultFilename(e.target.value)}
            rightElement={<span className="bp4-text-muted">.yaml</span>}
          />
        </FormGroup>

        <FormGroup label="Select Task Sources">
          <div className="task-sources-list">
            {taskSources.map((source, index) => (
              <Checkbox
                key={source.name}
                checked={source.selected}
                label={source.name}
                onChange={() => {
                  const newSources = [...taskSources];
                  newSources[index] = {
                    ...source,
                    selected: !source.selected
                  };
                  setTaskSources(newSources);
                }}
              />
            ))}
          </div>
        </FormGroup>
      </div>

      <div className={Classes.DIALOG_FOOTER}>
        <div className={Classes.DIALOG_FOOTER_ACTIONS}>
          <Button onClick={onClose}>Cancel</Button>
          <Button
            intent="primary"
            onClick={handleRun}
            loading={isLoading}
          >
            Run Benchmark
          </Button>
        </div>
      </div>
    </Dialog>
  );
}
