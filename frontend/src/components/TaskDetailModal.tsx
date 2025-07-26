import { useState } from 'react';
import { Dialog, Classes, Button, FormGroup, InputGroup, Card, Tabs, Tab } from '@blueprintjs/core';
import Editor from '@monaco-editor/react';
import { parse, stringify } from 'yaml';
import styles from './TaskSource.module.css';

interface Props {
  isOpen: boolean;
  onClose: () => void;
  task: any;
  taskIndex: number;
  onTaskUpdate: (index: number, updatedTask: any) => void;
}

export function TaskDetailModal({ isOpen, onClose, task, taskIndex, onTaskUpdate }: Props) {
  const [localTask, setLocalTask] = useState(task);
  const [selectedTabId, setSelectedTabId] = useState('basic');
  const [showCodeEditor, setShowCodeEditor] = useState(false);
  const [codeEditorContent, setCodeEditorContent] = useState('');
  const [codeEditorPath, setCodeEditorPath] = useState<string[]>([]);

  const updateField = (path: string[], value: any) => {
    setLocalTask(prev => {
      const newTask = { ...prev };
      let current = newTask;
      for (let i = 0; i < path.length - 1; i++) {
        if (!current[path[i]]) {
          current[path[i]] = {};
        }
        current = current[path[i]];
      }
      current[path[path.length - 1]] = value;
      return newTask;
    });
  };

  const handleSave = () => {
    onTaskUpdate(taskIndex, localTask);
    onClose();
  };

  const openCodeEditor = (path: string[], content: string) => {
    setCodeEditorPath(path);
    setCodeEditorContent(content);
    setShowCodeEditor(true);
  };

  const saveCodeEditorContent = () => {
    updateField(codeEditorPath, codeEditorContent);
    setShowCodeEditor(false);
  };

  const renderBasicInfo = () => (
    <Card>
      <FormGroup label="Name">
        <InputGroup
          value={localTask.name || ''}
          onChange={e => updateField(['name'], e.target.value)}
        />
      </FormGroup>
      <FormGroup label="Type">
        <InputGroup
          value={localTask.type || ''}
          onChange={e => updateField(['type'], e.target.value)}
        />
      </FormGroup>
      <FormGroup label="Difficulty">
        <InputGroup
          value={localTask.difficulty || ''}
          onChange={e => updateField(['difficulty'], e.target.value)}
        />
      </FormGroup>
      <FormGroup label="Area">
        <InputGroup
          value={localTask.area || ''}
          onChange={e => updateField(['area'], e.target.value)}
        />
      </FormGroup>
      <FormGroup label="Source">
        <InputGroup
          value={localTask.source || ''}
          onChange={e => updateField(['source'], e.target.value)}
        />
      </FormGroup>
    </Card>
  );

  const renderLanguages = () => (
    <Card>
      {Object.entries(localTask.languages_specific || {}).map(([lang, config]: [string, any]) => (
        <Card key={lang} style={{ marginBottom: '1rem' }}>
          <h4>{lang}</h4>
          <Button
            text="Edit Description"
            onClick={() => openCodeEditor(['languages_specific', lang, 'description'], config.description || '')}
          />
          <Button
            text="Edit Public Tests"
            onClick={() => openCodeEditor(
              ['languages_specific', lang, 'public_tests'],
              stringify(config.public_tests || [])
            )}
          />
          <Button
            text="Edit Hidden Tests"
            onClick={() => openCodeEditor(
              ['languages_specific', lang, 'hidden_tests'],
              stringify(config.hidden_tests || [])
            )}
          />
        </Card>
      ))}
    </Card>
  );

  const renderPrompt = () => (
    <Card>
      <Button
        text="Edit Common Prompt"
        onClick={() => openCodeEditor(['task', 'common_prompt'], localTask.task?.common_prompt || '')}
      />
      {Object.entries(localTask.task || {}).map(([key, value]) => {
        if (key === 'common_prompt') return null;
        return (
          <Button
            key={key}
            text={`Edit ${key}`}
            onClick={() => openCodeEditor(['task', key], stringify(value))}
          />
        );
      })}
    </Card>
  );

  return (
    <>
      <Dialog
        isOpen={isOpen}
        onClose={onClose}
        title="Edit Task"
        style={{ width: '90vw', maxHeight: '90vh' }}
      >
        <div className={Classes.DIALOG_BODY}>
          <Tabs
            id="taskTabs"
            selectedTabId={selectedTabId}
            onChange={setSelectedTabId as any}
          >
            <Tab id="basic" title="Basic Info" panel={renderBasicInfo()} />
            <Tab id="languages" title="Languages" panel={renderLanguages()} />
            <Tab id="prompt" title="Task Prompt" panel={renderPrompt()} />
          </Tabs>
        </div>
        <div className={Classes.DIALOG_FOOTER}>
          <div className={Classes.DIALOG_FOOTER_ACTIONS}>
            <Button onClick={onClose}>Cancel</Button>
            <Button intent="primary" onClick={handleSave}>Save</Button>
          </div>
        </div>
      </Dialog>

      <Dialog
        isOpen={showCodeEditor}
        onClose={() => setShowCodeEditor(false)}
        title="Code Editor"
        style={{ width: '90vw', height: '90vh' }}
      >
        <div className={Classes.DIALOG_BODY} style={{ height: 'calc(90vh - 120px)' }}>
          <Editor
            height="100%"
            defaultLanguage={codeEditorPath.includes('tests') ? 'yaml' : 'plaintext'}
            value={codeEditorContent}
            onChange={value => setCodeEditorContent(value || '')}
            options={{ minimap: { enabled: false } }}
          />
        </div>
        <div className={Classes.DIALOG_FOOTER}>
          <div className={Classes.DIALOG_FOOTER_ACTIONS}>
            <Button onClick={() => setShowCodeEditor(false)}>Cancel</Button>
            <Button intent="primary" onClick={saveCodeEditorContent}>Save</Button>
          </div>
        </div>
      </Dialog>
    </>
  );
}
