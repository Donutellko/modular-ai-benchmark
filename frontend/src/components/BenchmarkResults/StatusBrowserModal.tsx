import React, { useState } from 'react';
import { Dialog, Tabs, Tab, HTMLTable, Button } from '@blueprintjs/core';
import { TaskResultModal } from './TaskResultModal';
import './BenchmarkResults.css';

interface Entry {
    taskName: string;
    result: {
        criteria: string;
        score: number;
        unit: string;
        error?: string;
        output?: string;
        time_millis: number;
    };
    llmResponse: any;
}

interface Props {
    isOpen: boolean;
    onClose: () => void;
    modelName: string;
    taskSourceName: string;
    criteria: string;
    type: 'complete' | 'skipped' | 'errors';
    entries: Entry[];
    onEntryClick: (entry: Entry) => void;
}

export const StatusBrowserModal: React.FC<Props> = ({
    isOpen,
    onClose,
    modelName,
    taskSourceName,
    criteria,
    type,
    entries,
    onEntryClick
}) => {
    const [taskModalData, setTaskModalData] = useState<{entry: Entry} | null>(null);

    // Filter entries based on type
    const completeEntries = entries.filter(entry => !entry.result.error && entry.result.score >= 0);
    const skippedEntries = entries.filter(entry => !entry.result.error && entry.result.score < 0);
    const errorEntries = entries.filter(entry => !!entry.result.error);

    const handleTaskNameClick = (entry: Entry) => {
        setTaskModalData({ entry });
    };

    const handleTaskModalClose = () => {
        setTaskModalData(null);
    };

    const renderTaskButton = (entry: Entry) => (
        <Button
            minimal
            onClick={() => handleTaskNameClick(entry)}
        >
            {entry.taskName}
        </Button>
    );

    return (
        <>
            <Dialog
                isOpen={isOpen}
                onClose={onClose}
                title={`Status Browser: ${modelName} - ${taskSourceName}`}
                className="status-browser-modal"
                style={{ width: '90%', maxWidth: '1000px' }}
            >
                <div className="bp3-dialog-body">
                    <Tabs id="status-tabs" defaultSelectedTabId={type}>
                        <Tab
                            id="complete"
                            title={`Complete (${completeEntries.length})`}
                            panel={
                                <HTMLTable striped interactive style={{ width: '100%' }}>
                                    <thead>
                                    <tr>
                                        <th>Task Name</th>
                                        <th>Score</th>
                                        <th>Unit</th>
                                        <th>Time (ms)</th>
                                        <th>Actions</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {completeEntries.map((entry, index) => (
                                        <tr key={index}>
                                            <td>{renderTaskButton(entry)}</td>
                                            <td>{entry.result.score.toFixed(2)}</td>
                                            <td>{entry.result.unit}</td>
                                            <td>{entry.result.time_millis?.toFixed(2) || 'N/A'}</td>
                                            <td>
                                                <Button
                                                    small
                                                    minimal
                                                    icon="info-sign"
                                                    onClick={() => onEntryClick(entry)}
                                                >
                                                    Details
                                                </Button>
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </HTMLTable>
                            }
                        />
                        <Tab
                            id="skipped"
                            title={`Skipped (${skippedEntries.length})`}
                            panel={
                                <HTMLTable striped interactive style={{ width: '100%' }}>
                                    <thead>
                                    <tr>
                                        <th>Task Name</th>
                                        <th>Score</th>
                                        <th>Unit</th>
                                        <th>Time (ms)</th>
                                        <th>Actions</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {skippedEntries.map((entry, index) => (
                                        <tr key={index}>
                                            <td>{renderTaskButton(entry)}</td>
                                            <td>{entry.result.score.toFixed(2)}</td>
                                            <td>{entry.result.unit}</td>
                                            <td>{entry.result.time_millis?.toFixed(2) || 'N/A'}</td>
                                            <td>
                                                <Button
                                                    small
                                                    minimal
                                                    icon="info-sign"
                                                    onClick={() => onEntryClick(entry)}
                                                >
                                                    Details
                                                </Button>
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </HTMLTable>
                            }
                        />
                        <Tab
                            id="errors"
                            title={`Errors (${errorEntries.length})`}
                            panel={
                                <HTMLTable striped interactive style={{ width: '100%' }}>
                                    <thead>
                                    <tr>
                                        <th>Task Name</th>
                                        <th>Error</th>
                                        <th>Time (ms)</th>
                                        <th>Actions</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {errorEntries.map((entry, index) => (
                                        <tr key={index}>
                                            <td>{renderTaskButton(entry)}</td>
                                            <td>
                                                {entry.result.error
                                                    ? entry.result.error.substring(0, 100) + (entry.result.error.length > 100 ? '...' : '')
                                                    : 'N/A'
                                                }
                                            </td>
                                            <td>{entry.result.time_millis?.toFixed(2) || 'N/A'}</td>
                                            <td>
                                                <Button
                                                    small
                                                    minimal
                                                    icon="info-sign"
                                                    onClick={() => onEntryClick(entry)}
                                                >
                                                    Details
                                                </Button>
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </HTMLTable>
                            }
                        />
                    </Tabs>
                </div>
                <div className="bp3-dialog-footer">
                    <div className="bp3-dialog-footer-actions">
                        <Button onClick={onClose}>Close</Button>
                    </div>
                </div>
            </Dialog>

            {taskModalData && (
                <TaskResultModal
                    isOpen={true}
                    onClose={handleTaskModalClose}
                    taskName={taskModalData.entry.taskName}
                    modelName={modelName}
                    taskSourceName={taskSourceName}
                    llmResponse={taskModalData.entry.llmResponse}
                    onCriteriaClick={(criteria) => {
                        // Handle criteria click if needed
                    }}
                    allCriteria={[taskModalData.entry.result]}
                />
            )}
        </>
    );
};
