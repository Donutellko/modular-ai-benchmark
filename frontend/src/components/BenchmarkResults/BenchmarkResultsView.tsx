import React, { useEffect, useState } from 'react';
import { Card, Button, HTMLSelect, NonIdealState, Spinner, Collapse, H3, H4 } from '@blueprintjs/core';
import { api } from '../../services/api';
import { CriteriaStatsTable } from './CriteriaStatsTable';
import './BenchmarkResults.css';

interface Props {
    selectedFile: string | null;
    onFileSelect: (file: string | null) => void;
}

// Define interfaces for the data structure
interface CriteriaResult {
    criteria: string;
    score: number;
    unit: string;
    error?: string;
    output?: string;
    time_millis: number;
    executor_class: string;
    prepared_code?: string;
    test_number?: number;
    exit_code?: number;
}

interface TaskResult {
    language: string;
    provider_name: string;
    model_name: string;
    llm_response: {
        model_name: string;
        prompt: string;
        language: string;
        response_text: string;
        token_count: number;
        prompt_token_count: number;
        time_millis: number;
    };
    evaluation_result: CriteriaResult[];
}

interface TaskDetails {
    task_source_path: string;
    task_source_name: string;
    task_definition_name: string;
    skip_reasons: string[];
    task_results: TaskResult[];
}

interface Result {
    area: string;
    name: string;
    details: TaskDetails;
}

interface BenchmarkEntry {
    results: Result[];
    'task-source-name': string;
    'task-source-path': string;
}

export const BenchmarkResultsView: React.FC<Props> = ({ selectedFile, onFileSelect }) => {
    const [rawData, setRawData] = useState<BenchmarkEntry[]>([]);
    const [loading, setLoading] = useState(true);
    const [availableFiles, setAvailableFiles] = useState<string[]>([]);
    const [error, setError] = useState<string | null>(null);
    const [openSections, setOpenSections] = useState<{[key: string]: boolean}>({});

    // For FileList - benchmark results are read-only
    const modifiedFiles = new Set<string>();

    useEffect(() => {
        loadAvailableFiles();
    }, []);

    useEffect(() => {
        if (selectedFile) {
            loadResults(selectedFile);
        }
    }, [selectedFile]);

    const loadAvailableFiles = async () => {
        try {
            const files = await api.listFiles('bench_results');
            setAvailableFiles(files);
            if (files.length > 0 && !selectedFile) {
                onFileSelect(files[0]);
            } else {
                setLoading(false);
            }
        } catch (err) {
            setError('Failed to load benchmark result files');
            setLoading(false);
        }
    };

    const loadResults = async (filename: string) => {
        try {
            setLoading(true);
            setError(null);
            const data = await api.getFile('bench_results', filename);
            // Parse YAML string to object
            const yamlData = await api.parseYaml(data);
            setRawData(yamlData);
            setLoading(false);
        } catch (err) {
            setError('Failed to load benchmark results');
            setRawData([]);
            setLoading(false);
        }
    };

    // Group results by model name and task source
    const getGroupedResults = () => {
        const modelMap: {[key: string]: {[key: string]: Result[]}} = {};

        rawData.forEach(entry => {
            entry.results.forEach(result => {
                result.details.task_results.forEach(taskResult => {
                    const modelName = taskResult.model_name;
                    const taskSourceName = result.details.task_source_name;

                    if (!modelMap[modelName]) {
                        modelMap[modelName] = {};
                    }

                    if (!modelMap[modelName][taskSourceName]) {
                        modelMap[modelName][taskSourceName] = [];
                    }

                    modelMap[modelName][taskSourceName].push(result);
                });
            });
        });

        return modelMap;
    };

    const toggleSection = (sectionKey: string) => {
        setOpenSections(prev => ({
            ...prev,
            [sectionKey]: !prev[sectionKey]
        }));
    };

    if (error) {
        return (
            <NonIdealState
                icon="error"
                title="Error Loading Results"
                description={error}
                action={<Button onClick={() => loadAvailableFiles()}>Retry</Button>}
            />
        );
    }

    if (loading && !rawData.length) {
        return (
            <NonIdealState
                icon={<Spinner />}
                title="Loading Results"
                description="Please wait while we load the benchmark results..."
            />
        );
    }

    if (!selectedFile && availableFiles.length === 0) {
        return (
            <NonIdealState
                icon="folder-open"
                title="No Results Available"
                description="No benchmark result files found."
            />
        );
    }

    const groupedResults = getGroupedResults();

    return (
        <Card className="benchmark-results">
            <div style={{ marginBottom: '20px', display: 'flex', gap: '10px', alignItems: 'center' }}>
                <HTMLSelect
                    value={selectedFile || ''}
                    onChange={(e) => onFileSelect(e.target.value)}
                    options={availableFiles.map(file => ({ value: file, label: file }))}
                    fill={true}
                />
                <Button
                    icon="refresh"
                    onClick={() => selectedFile && loadResults(selectedFile)}
                    loading={loading}
                />
            </div>

            {Object.keys(groupedResults).length === 0 && !loading ? (
                <NonIdealState
                    icon="document"
                    title="No Results"
                    description="This benchmark result file is empty or has invalid format."
                />
            ) : (
                <div className="benchmark-results-content">
                    {Object.entries(groupedResults).map(([modelName, taskSources]) => (
                        <div key={modelName} className="model-section">
                            <H3
                                className="model-header"
                                onClick={() => toggleSection(`model-${modelName}`)}
                            >
                                {modelName} <Button minimal small icon={openSections[`model-${modelName}`] ? "chevron-down" : "chevron-right"} />
                            </H3>
                            <Collapse isOpen={!!openSections[`model-${modelName}`]}>
                                {Object.entries(taskSources).map(([taskSourceName, results]) => (
                                    <div key={taskSourceName} className="task-source-section">
                                        <H4
                                            className="task-source-header"
                                            onClick={() => toggleSection(`${modelName}-${taskSourceName}`)}
                                        >
                                            {taskSourceName} <Button minimal small icon={openSections[`${modelName}-${taskSourceName}`] ? "chevron-down" : "chevron-right"} />
                                        </H4>
                                        <Collapse isOpen={!!openSections[`${modelName}-${taskSourceName}`]}>
                                            <CriteriaStatsTable
                                                results={results}
                                                modelName={modelName}
                                                taskSourceName={taskSourceName}
                                            />
                                        </Collapse>
                                    </div>
                                ))}
                            </Collapse>
                        </div>
                    ))}
                </div>
            )}
        </Card>
    );
};
