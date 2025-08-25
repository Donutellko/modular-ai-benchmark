import React from 'react';
import { Dialog, Button, HTMLTable } from '@blueprintjs/core';
import { Editor } from '@monaco-editor/react';
import { TaskResultModal } from './TaskResultModal';
import './BenchmarkResults.css';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    entry: {
        taskName: string;
        result: {
            criteria: string;
            score: number;
            unit: string;
            error?: string;
            output?: string;
            time_millis: number;
            executor_class: string;
            prepared_code?: string;
        };
        llmResponse: {
            model_name: string;
            prompt: string;
            language: string;
            response_text: string;
        };
    };
    modelName: string;
    taskSourceName: string;
}

export const CriteriaResultModal: React.FC<Props> = ({
    isOpen,
    onClose,
    entry,
    modelName,
    taskSourceName
}) => {
    const [taskModalOpen, setTaskModalOpen] = React.useState(false);

    const getLanguageForEditor = (lang: string) => {
        // Map language names to Monaco editor language identifiers
        const langMap: {[key: string]: string} = {
            'java': 'java',
            'javascript': 'javascript',
            'python': 'python',
            'c': 'c',
            'cpp': 'cpp',
            'csharp': 'csharp',
            'go': 'go',
            'rust': 'rust',
        };

        return langMap[lang.toLowerCase()] || 'plaintext';
    };

    // Helper function to format score based on criteria type
    const formatScore = (score: number, criteria: string) => {
        if (criteria === 'unit-test') {
            return `${(score * 100).toFixed(0)}%`;
        }
        return score.toFixed(2);
    };

    return (
        <>
            <Dialog
                isOpen={isOpen}
                onClose={onClose}
                title={`Criteria Result: ${modelName} - ${taskSourceName} - ${entry.taskName} - ${entry.result.criteria}`}
                className="criteria-result-modal"
                style={{ width: '90%', maxWidth: '1200px' }}
            >
                <div className="bp3-dialog-body">
                    <div className="criteria-result-section">
                        <h3>Basic Information</h3>
                        <HTMLTable>
                            <tbody>
                                <tr>
                                    <td><strong>Task Name</strong></td>
                                    <td>
                                        <Button
                                            minimal
                                            onClick={() => setTaskModalOpen(true)}
                                        >
                                            {entry.taskName}
                                        </Button>
                                    </td>
                                </tr>
                                <tr>
                                    <td><strong>Model</strong></td>
                                    <td>{modelName}</td>
                                </tr>
                                <tr>
                                    <td><strong>Task Source</strong></td>
                                    <td>{taskSourceName}</td>
                                </tr>
                                <tr>
                                    <td><strong>Criteria</strong></td>
                                    <td>{entry.result.criteria}</td>
                                </tr>
                                <tr>
                                    <td><strong>Executor Class</strong></td>
                                    <td>{entry.result.executor_class}</td>
                                </tr>
                            </tbody>
                        </HTMLTable>
                    </div>

                    <div className="criteria-result-section">
                        <h3>Evaluation Result</h3>
                        <HTMLTable>
                            <tbody>
                                <tr>
                                    <td><strong>Score</strong></td>
                                    <td>{formatScore(entry.result.score, entry.result.criteria)}</td>
                                </tr>
                                <tr>
                                    <td><strong>Unit of Measure</strong></td>
                                    <td>{entry.result.unit}</td>
                                </tr>
                                <tr>
                                    <td><strong>Execution Time (ms)</strong></td>
                                    <td>{entry.result.time_millis?.toFixed(2) || 'N/A'}</td>
                                </tr>
                                {entry.result.error && (
                                    <tr>
                                        <td><strong>Error</strong></td>
                                        <td style={{ color: 'red' }}>{entry.result.error}</td>
                                    </tr>
                                )}
                                {entry.result.output && (
                                    <tr>
                                        <td><strong>Output</strong></td>
                                        <td>
                                            <div style={{ maxHeight: '200px', overflow: 'auto' }}>
                                                <pre>{entry.result.output}</pre>
                                            </div>
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </HTMLTable>
                    </div>

                    <div className="criteria-result-section">
                        <h3>Prompt</h3>
                        <Editor
                            height="200px"
                            defaultLanguage={getLanguageForEditor(entry.llmResponse.language)}
                            value={entry.llmResponse.prompt}
                            options={{
                                readOnly: true,
                                minimap: { enabled: false },
                                scrollBeyondLastLine: false
                            }}
                        />
                    </div>

                    <div className="criteria-result-section">
                        <h3>Response</h3>
                        <Editor
                            height="200px"
                            defaultLanguage={getLanguageForEditor(entry.llmResponse.language)}
                            value={entry.llmResponse.response_text}
                            options={{
                                readOnly: true,
                                minimap: { enabled: false },
                                scrollBeyondLastLine: false
                            }}
                        />
                    </div>

                    {entry.result.prepared_code && (
                        <div className="criteria-result-section">
                            <h3>Prepared Code</h3>
                            <Editor
                                height="300px"
                                defaultLanguage={getLanguageForEditor(entry.llmResponse.language)}
                                value={entry.result.prepared_code}
                                options={{
                                    readOnly: true,
                                    minimap: { enabled: false },
                                    scrollBeyondLastLine: false
                                }}
                            />
                        </div>
                    )}
                </div>
                <div className="bp3-dialog-footer">
                    <div className="bp3-dialog-footer-actions">
                        <Button onClick={onClose}>Close</Button>
                    </div>
                </div>
            </Dialog>

            {taskModalOpen && (
                <TaskResultModal
                    isOpen={taskModalOpen}
                    onClose={() => setTaskModalOpen(false)}
                    taskName={entry.taskName}
                    modelName={modelName}
                    taskSourceName={taskSourceName}
                    llmResponse={entry.llmResponse}
                    onCriteriaClick={(criteria) => {
                        // Since we're already in criteria view, this doesn't need to do anything
                        // but in a real app you might navigate to other criteria
                    }}
                    // This would need actual data for all criteria
                    allCriteria={[entry.result]}
                />
            )}
        </>
    );
};
