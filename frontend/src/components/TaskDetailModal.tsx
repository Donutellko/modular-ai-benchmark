import { useState, useEffect } from 'react';
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
  const [localTask, setLocalTask] = useState({...task});  // Create a deep copy
  const [selectedTabId, setSelectedTabId] = useState('basic');
  const [showCodeEditor, setShowCodeEditor] = useState(false);
  const [codeEditorContent, setCodeEditorContent] = useState('');
  const [codeEditorPath, setCodeEditorPath] = useState<string[]>([]);

  useEffect(() => {
    setLocalTask({...task}); // Update local task when prop changes
  }, [task]);

  const updateField = (path: string[], value: any) => {
    setLocalTask(prev => {
      const result = {...prev};
      let current = result;
      for (let i = 0; i < path.length - 1; i++) {
        if (!current[path[i]]) {
          current[path[i]] = {};
        }
        current = current[path[i]];
      }
      current[path[path.length - 1]] = value;
      return result;
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

  const renderPromptTab = () => (
    <Card>
      <div style={{ marginBottom: '20px' }}>
        <h4>Common Prompt Template</h4>
        <Button
          icon="edit"
          text="Edit Common Prompt"
          onClick={() => openCodeEditor(['task', 'common_prompt'], localTask.task?.common_prompt || '')}
          style={{ marginBottom: '10px' }}
        />

        <h4 style={{ marginTop: '20px' }}>Language-Specific Additions</h4>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          {Object.entries(localTask.task?.languages_specific || {}).map(([lang, config]: [string, any]) => (
            <Card key={lang}>
              <h5>{lang}</h5>
              <Button
                icon="edit"
                text="Edit Language-Specific Prompt"
                onClick={() => openCodeEditor(['task', 'languages_specific', lang, 'description'], config.description || '')}
              />
            </Card>
          ))}
        </div>

        <div style={{ marginTop: '20px', color: '#5C7080' }}>
          <p>Available template variables:</p>
          <ul>
            <li>${'{language}'} - will be replaced with the target programming language</li>
            <li>${'{common_prompt}'} - will be replaced with the common prompt (in language-specific sections)</li>
            <li>${'{public_tests}'} - will be replaced with public test cases</li>
            <li>${'{hidden_tests}'} - will be replaced with hidden test cases (if enabled)</li>
            <li>${'parameters[\'parameter-name\']'} - will be replaced with parameter values</li>
          </ul>
        </div>
      </div>
    </Card>
  );

  return (
    <>
      <Dialog
        isOpen={isOpen}
        onClose={onClose}
        title={`Edit Task: ${localTask.name}`}
        style={{ width: '90vw', maxWidth: '1200px' }}
      >
        <div className={Classes.DIALOG_BODY}>
          <Tabs
            id="task-tabs"
            selectedTabId={selectedTabId}
            onChange={(newTabId) => setSelectedTabId(newTabId.toString())}
          >
            <Tab id="basic" title="Basic Info" panel={renderBasicInfo()} />
            <Tab id="prompt" title="Task Prompt" panel={renderPromptTab()} />
            <Tab id="languages" title="Languages" panel={renderLanguages()} />
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
        title="Edit Code"
        style={{ width: '90vw', height: '80vh' }}
      >
        <div className={Classes.DIALOG_BODY} style={{ height: 'calc(100% - 100px)' }}>
          <Editor
            height="100%"
            defaultLanguage="yaml"
            value={codeEditorContent}
            onChange={(value) => setCodeEditorContent(value || '')}
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
