import { useState, useEffect } from 'react';
import { HTMLTable, Button, EditableText } from '@blueprintjs/core';
import { parse, stringify } from 'yaml';
import { TaskDetailModal } from './TaskDetailModal';

interface Task {
  name: string;
  difficulty: string;
  source: string;
  languages: string[];
  type: string;
  area: string;
}

interface TaskSourceFile {
  version: string;
  name: string;
  tasks: Task[];
}

interface Props {
  content: string;
  onContentChange: (newContent: string) => void;
}

export function TaskSourceEditor({ content, onContentChange }: Props) {
  const [parsedContent, setParsedContent] = useState<TaskSourceFile | null>(null);
  const [editingTask, setEditingTask] = useState<{index: number, task: any} | null>(null);

  useEffect(() => {
    try {
      const parsed = parse(content);
      setParsedContent({
        version: parsed.version || '1.0',
        name: parsed.name || '',
        tasks: Array.isArray(parsed.tasks) ? parsed.tasks : []
      });
    } catch (e) {
      console.error('Failed to parse YAML:', e);
      setParsedContent({
        version: '1.0',
        name: '',
        tasks: []
      });
    }
  }, [content]);

  const updateTask = (index: number, field: keyof Task, value: any) => {
    if (!parsedContent) return;

    const newContent = {...parsedContent};
    newContent.tasks = [...parsedContent.tasks];
    newContent.tasks[index] = {
      ...newContent.tasks[index],
      [field]: value
    };

    setParsedContent(newContent);
    onContentChange(stringify(newContent));
  };

  const handleTaskUpdate = (index: number, updatedTask: Task) => {
    if (!parsedContent) return;

    const newContent = {...parsedContent};
    newContent.tasks = [...parsedContent.tasks];
    newContent.tasks[index] = updatedTask;

    setParsedContent(newContent);
    onContentChange(stringify(newContent));
  };

  if (!parsedContent) {
    return <div>Loading...</div>;
  }

  return (
    <div className="task-source-editor">
      <div className="header-actions">
        <Button icon="plus" text="Add Task" onClick={() => {
          const newTask = {
            name: "New Task",
            type: "implementation from zero",
            difficulty: "easy",
            area: "general",
            source: "custom",
            languages: ["python", "java"],
            available_parameters: [],
            available_criteria: []
          };
          const newContent = {
            ...parsedContent,
            tasks: [...parsedContent.tasks, newTask]
          };
          setParsedContent(newContent);
          onContentChange(stringify(newContent));
        }} />
      </div>

      <HTMLTable interactive striped>
        <thead>
          <tr>
            <th>Task Name</th>
            <th>Difficulty</th>
            <th>Source</th>
            <th>Languages</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {parsedContent.tasks.map((task, index) => (
            <tr key={index}>
              <td>
                <EditableText
                  value={task.name}
                  onChange={value => updateTask(index, 'name', value)}
                />
              </td>
              <td>
                <EditableText
                  value={task.difficulty}
                  onChange={value => updateTask(index, 'difficulty', value)}
                />
              </td>
              <td>
                <EditableText
                  value={task.source}
                  onChange={value => updateTask(index, 'source', value)}
                />
              </td>
              <td>
                {task.languages.join(', ')}
              </td>
              <td>
                <Button
                  icon="edit"
                  minimal
                  onClick={() => setEditingTask({ index, task })}
                />
                <Button icon="trash" minimal intent="danger" onClick={() => {
                  const newTasks = parsedContent.tasks.filter((_, i) => i !== index);
                  const newContent = {...parsedContent, tasks: newTasks};
                  setParsedContent(newContent);
                  onContentChange(stringify(newContent));
                }} />
              </td>
            </tr>
          ))}
        </tbody>
      </HTMLTable>

      {editingTask && (
        <TaskDetailModal
          isOpen={true}
          onClose={() => setEditingTask(null)}
          task={editingTask.task}
          taskIndex={editingTask.index}
          onTaskUpdate={handleTaskUpdate}
        />
      )}
    </div>
  );
}
