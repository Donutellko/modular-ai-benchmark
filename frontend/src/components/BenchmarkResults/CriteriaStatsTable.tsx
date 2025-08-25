import React, { useState, useMemo } from 'react';
import { HTMLTable, Button } from '@blueprintjs/core';
import { StatusBrowserModal } from './StatusBrowserModal';
import { CriteriaResultModal } from './CriteriaResultModal';
import './BenchmarkResults.css';

interface CriteriaResult {
    criteria: string;
    score: number;
    unit: string;
    error?: string;
    output?: string;
    time_millis: number;
    executor_class: string;
    prepared_code?: string;
}

interface TaskResult {
    language: string;
    provider_name: string;
    model_name: string;
    llm_response: any;
    evaluation_result: CriteriaResult[];
}

interface Result {
    area: string;
    name: string;
    details: {
        task_source_path: string;
        task_source_name: string;
        task_definition_name: string;
        skip_reasons: string[];
        task_results: TaskResult[];
    };
}

interface CriteriaStats {
    criteria: string;
    avgScore: number;
    complete: number;
    skipped: number;
    errors: number;
    results: Array<{
        taskName: string;
        result: CriteriaResult;
        llmResponse: any;
    }>;
}

interface Props {
    results: Result[];
    modelName: string;
    taskSourceName: string;
}

export const CriteriaStatsTable: React.FC<Props> = ({ results, modelName, taskSourceName }) => {
    const [statusModalType, setStatusModalType] = useState<'complete' | 'skipped' | 'errors' | null>(null);
    const [selectedCriteria, setSelectedCriteria] = useState<string | null>(null);
    const [selectedCriteriaEntry, setSelectedCriteriaEntry] = useState<any | null>(null);

    // Calculate statistics for each criteria
    const criteriaStats = useMemo(() => {
        const statsMap: Map<string, CriteriaStats> = new Map();

        results.forEach(result => {
            const taskName = result.name;

            result.details.task_results.forEach(taskResult => {
                if (taskResult.model_name === modelName) {
                    taskResult.evaluation_result.forEach(evalResult => {
                        const { criteria } = evalResult;

                        if (!statsMap.has(criteria)) {
                            statsMap.set(criteria, {
                                criteria,
                                avgScore: 0,
                                complete: 0,
                                skipped: 0,
                                errors: 0,
                                results: []
                            });
                        }

                        const stats = statsMap.get(criteria)!;

                        // Add to appropriate counter
                        if (evalResult.error) {
                            stats.errors++;
                        } else if (evalResult.score < 0) {
                            stats.skipped++;
                        } else {
                            stats.complete++;
                            // Only calculate average for non-negative scores
                            stats.avgScore = stats.avgScore + evalResult.score;
                        }

                        // Store the result details for later use
                        stats.results.push({
                            taskName,
                            result: evalResult,
                            llmResponse: taskResult.llm_response
                        });
                    });
                }
            });
        });

        // Calculate final averages
        statsMap.forEach(stats => {
            if (stats.complete > 0) {
                stats.avgScore = stats.avgScore / stats.complete;
            }
        });

        return Array.from(statsMap.values());
    }, [results, modelName]);

    const handleStatusClick = (type: 'complete' | 'skipped' | 'errors', criteria: string) => {
        setStatusModalType(type);
        setSelectedCriteria(criteria);
    };

    const handleCloseStatusModal = () => {
        setStatusModalType(null);
        setSelectedCriteria(null);
    };

    const handleCriteriaEntryClick = (entry: any) => {
        setSelectedCriteriaEntry(entry);
    };

    const handleCloseCriteriaModal = () => {
        setSelectedCriteriaEntry(null);
    };

    return (
        <div className="criteria-stats-table">
            <HTMLTable striped>
                <thead>
                    <tr>
                        <th>Criteria</th>
                        <th>Avg. Score</th>
                        <th>Complete</th>
                        <th>Skipped</th>
                        <th>Errors</th>
                    </tr>
                </thead>
                <tbody>
                    {criteriaStats.map((stats) => (
                        <tr key={stats.criteria}>
                            <td>{stats.criteria}</td>
                            <td>{stats.avgScore.toFixed(2)}</td>
                            <td>
                                <Button
                                    minimal
                                    onClick={() => handleStatusClick('complete', stats.criteria)}
                                    disabled={stats.complete === 0}
                                >
                                    {stats.complete}
                                </Button>
                            </td>
                            <td>
                                <Button
                                    minimal
                                    onClick={() => handleStatusClick('skipped', stats.criteria)}
                                    disabled={stats.skipped === 0}
                                >
                                    {stats.skipped}
                                </Button>
                            </td>
                            <td>
                                <Button
                                    minimal
                                    onClick={() => handleStatusClick('errors', stats.criteria)}
                                    disabled={stats.errors === 0}
                                >
                                    {stats.errors}
                                </Button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </HTMLTable>

            {statusModalType && selectedCriteria && (
                <StatusBrowserModal
                    isOpen={!!statusModalType}
                    onClose={handleCloseStatusModal}
                    modelName={modelName}
                    taskSourceName={taskSourceName}
                    criteria={selectedCriteria}
                    type={statusModalType}
                    entries={criteriaStats.find(stats => stats.criteria === selectedCriteria)?.results || []}
                    onEntryClick={handleCriteriaEntryClick}
                />
            )}

            {selectedCriteriaEntry && (
                <CriteriaResultModal
                    isOpen={!!selectedCriteriaEntry}
                    onClose={handleCloseCriteriaModal}
                    entry={selectedCriteriaEntry}
                    modelName={modelName}
                    taskSourceName={taskSourceName}
                />
            )}
        </div>
    );
};

