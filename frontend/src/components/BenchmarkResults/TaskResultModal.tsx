// import React from 'react';
import { Dialog, Button, HTMLTable, Icon } from '@blueprintjs/core';
import { Editor } from '@monaco-editor/react';
import './BenchmarkResults.css';

interface CriteriaResult {
    criteria: string;
    score: number;
    unit: string;
    error?: string;
    output?: string;
    time_millis: number;
    executor_class?: string;
    prepared_code?: string;
}

interface Props {
    isOpen: boolean;
    onClose: () => void;
    taskName: string;
    modelName: string;
    taskSourceName: string;
    llmResponse: {
        model_name: string;
        prompt: string;
        language: string;
        response_text: string;
        timestamp?: string;
    };
    allCriteria: CriteriaResult[];
    onCriteriaClick: (criteria: CriteriaResult) => void;
}

export const TaskResultModal: React.FC<Props> = ({
    isOpen,
    onClose,
    taskName,
    modelName,
    taskSourceName,
    llmResponse,
    allCriteria,
    onCriteriaClick
}) => {
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

    const renderStatusIcons = (criteria: CriteriaResult) => {
        const icons = [];

        // Green checkmark if no error
        if (!criteria.error) {
            icons.push(<Icon key="success" icon="tick-circle" intent="success" />);
        }

        // Yellow cross if score is negative
        if (criteria.score < 0) {
            icons.push(<Icon key="warning" icon="cross" intent="warning" />);
        }

        // Red cross if there is any error
        if (criteria.error) {
            icons.push(<Icon key="error" icon="cross" intent="danger" />);
        }

        // Question mark if there is output
        if (criteria.output) {
            icons.push(<Icon key="output" icon="help" intent="primary" />);
        }

        return <div className="status-icons">{icons}</div>;
    };

    return (
        <Dialog
            isOpen={isOpen}
            onClose={onClose}
            title={`Task Result: ${modelName} - ${taskSourceName} - ${taskName}`}
            className="task-result-modal"
            style={{ width: '90%', maxWidth: '1200px' }}
        >
            <div className="bp3-dialog-body">
                <div className="task-result-section">
                    <h3>Basic Information</h3>
                    <HTMLTable>
                        <tbody>
                            <tr>
                                <td><strong>Task Name</strong></td>
                                <td>{taskName}</td>
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
                                <td><strong>Timestamp</strong></td>
                                <td>{llmResponse.timestamp || 'N/A'}</td>
                            </tr>
                        </tbody>
                    </HTMLTable>
                </div>

                <div className="task-result-section">
                    <h3>Prompt</h3>
                    <Editor
                        height="200px"
                        defaultLanguage={getLanguageForEditor(llmResponse.language)}
                        value={llmResponse.prompt}
                        options={{
                            readOnly: true,
                            minimap: { enabled: false },
                            scrollBeyondLastLine: false
                        }}
                    />
                </div>

                <div className="task-result-section">
                    <h3>Response</h3>
                    <Editor
                        height="200px"
                        defaultLanguage={getLanguageForEditor(llmResponse.language)}
                        value={llmResponse.response_text}
                        options={{
                            readOnly: true,
                            minimap: { enabled: false },
                            scrollBeyondLastLine: false
                        }}
                    />
                </div>

                <div className="task-result-section">
                    <h3>Evaluation Results</h3>
                    <HTMLTable striped interactive style={{ width: '100%' }}>
                        <thead>
                            <tr>
                                <th>Criteria</th>
                                <th>Score</th>
                                <th>Unit</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {allCriteria.map((criteria, index) => (
                                <tr key={index}>
                                    <td>
                                        <Button
                                            minimal
                                            onClick={() => onCriteriaClick(criteria)}
                                        >
                                            {criteria.criteria}
                                        </Button>
                                    </td>
                                    <td>{criteria.score}</td>
                                    <td>{criteria.unit}</td>
                                    <td>{renderStatusIcons(criteria)}</td>
                                </tr>
                            ))}
                        </tbody>
                    </HTMLTable>
                </div>
            </div>
            <div className="bp3-dialog-footer">
                <div className="bp3-dialog-footer-actions">
                    <Button onClick={onClose}>Close</Button>
                </div>
            </div>
        </Dialog>
    );
};
