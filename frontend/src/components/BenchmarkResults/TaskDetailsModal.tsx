import React from 'react';
import { Dialog, Tab, Tabs, Button, HTMLTable } from '@blueprintjs/core';
import { Editor } from '@monaco-editor/react';
import './BenchmarkResults.css';

interface TaskResult {
    taskSourceName: string;
    taskName: string;
    languages: string[];
    domain: string;
    difficulty: string;
    isSkipped: boolean;
    hasErrors: boolean;
    details: {
        skipReasons: string[];
        task_results: Array<{
            language: string;
            modelName: string;
            llm_response: {
                response_text: string;
            };
            evaluation_result: Array<{
                criteria: string;
                score: number;
                time_millis: number;
                error?: string;
            }>;
        }>;
    };
}

interface Props {
    task: TaskResult;
    isOpen: boolean;
    onClose: () => void;
}

export const TaskDetailsModal: React.FC<Props> = ({ task, isOpen, onClose }) => {
    return (
        <Dialog
            isOpen={isOpen}
            onClose={onClose}
            title={`Task Details: ${task.taskName}`}
            className="task-details-modal"
        >
            <div className="task-details-content">
                <Tabs className="task-details-tabs">
                    <Tab
                        id="summary"
                        title="Summary"
                        panel={
                            <div>
                                <h3>Basic Information</h3>
                                <div className="task-results-section">
                                    <p><strong>Task Source:</strong> {task.taskSourceName}</p>
                                    <p><strong>Domain:</strong> {task.domain}</p>
                                    <p><strong>Difficulty:</strong> {task.difficulty}</p>
                                    <p><strong>Languages:</strong> {task.languages.join(', ')}</p>
                                    {task.details.skipReasons?.length > 0 && (
                                        <>
                                            <h4>Skip Reasons:</h4>
                                            <ul>
                                                {task.details.skipReasons.map((reason: string, idx: number) => (
                                                    <li key={idx}>{reason}</li>
                                                ))}
                                            </ul>
                                        </>
                                    )}
                                </div>
                            </div>
                        }
                    />
                    <Tab
                        id="results"
                        title="Results by Language"
                        panel={
                            <div>
                                {task.details.task_results.map((result: any, idx: number) => (
                                    <div key={idx} className="task-results-section">
                                        <h3>{result.language} - {result.modelName}</h3>
                                        <div className="code-editor-container">
                                            <h4>LLM Response:</h4>
                                            <Editor
                                                height="200px"
                                                defaultLanguage={result.language.toLowerCase()}
                                                value={result.llm_response.response_text}
                                                options={{
                                                    readOnly: true,
                                                    minimap: { enabled: false },
                                                    scrollBeyondLastLine: false
                                                }}
                                            />
                                        </div>
                                        <div>
                                            <h4>Evaluation Results:</h4>
                                            <HTMLTable
                                                interactive
                                                striped
                                                style={{ width: '100%' }}
                                            >
                                                <thead>
                                                    <tr>
                                                        <th>Criteria</th>
                                                        <th>Score</th>
                                                        <th>Time (ms)</th>
                                                        <th>Status</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    {result.evaluation_result.map((evalResult: any, evalIdx: number) => (
                                                        <tr key={evalIdx}>
                                                            <td>{evalResult.criteria}</td>
                                                            <td>{evalResult.score}</td>
                                                            <td>{evalResult.time_millis ? evalResult.time_millis.toFixed(2) : 'N/A'}</td>
                                                            <td>
                                                                {evalResult.error ? (
                                                                    <Button
                                                                        minimal
                                                                        intent="danger"
                                                                        icon="error"
                                                                        onClick={() => alert(evalResult.error)}
                                                                    >
                                                                        Error
                                                                    </Button>
                                                                ) : 'Success'}
                                                            </td>
                                                        </tr>
                                                    ))}
                                                </tbody>
                                            </HTMLTable>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        }
                    />
                </Tabs>
            </div>
        </Dialog>
    );
};
