import React, { useEffect, useState } from 'react';
import { Card, Button, HTMLSelect, NonIdealState, Spinner } from '@blueprintjs/core';
import { api } from '../../services/api';
import { BenchmarkResultsTable } from './BenchmarkResultsTable';
import './BenchmarkResults.css';

interface Props {
    selectedFile: string | null;
    onFileSelect: (file: string | null) => void;
}

export const BenchmarkResultsView: React.FC<Props> = ({ selectedFile, onFileSelect }) => {
    const [results, setResults] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [availableFiles, setAvailableFiles] = useState<string[]>([]);
    const [error, setError] = useState<string | null>(null);

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
            setResults(transformResults(yamlData));
        } catch (err) {
            setError('Failed to load benchmark results');
            setResults([]);
        } finally {
            setLoading(false);
        }
    };

    const transformResults = (data: any) => {
        if (!Array.isArray(data)) return [];

        return data.map(result => ({
            taskSourceName: result['task-source-name'],
            taskName: result.results[0].name,
            languages: result.results[0].details.task_results?.map((tr: any) => tr.language) || [],
            domain: result.results[0].details.task_definition?.domain || 'N/A',
            difficulty: result.results[0].details.task_definition?.difficulty || 'N/A',
            isSkipped: result.results[0].details.skip_reasons?.length > 0,
            hasErrors: result.results[0].details.task_results?.some((tr: any) =>
                tr.evaluation_result?.some((er: any) => er.error)
            ) || false,
            details: result.results[0].details
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

    if (loading && !results.length) {
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

            {results.length > 0 ? (
                <BenchmarkResultsTable results={results} />
            ) : !loading && (
                <NonIdealState
                    icon="document"
                    title="No Results"
                    description="This benchmark result file is empty or has invalid format."
                />
            )}
        </Card>
    );
};
