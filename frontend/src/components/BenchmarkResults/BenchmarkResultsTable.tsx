import React, { useState } from 'react';
import { HTMLTable, InputGroup, Button } from '@blueprintjs/core';
import { TaskDetailsModal } from './TaskDetailsModal';
import './BenchmarkResults.css';

interface TaskResult {
    taskSourceName: string;
    taskName: string;
    languages: string[];
    domain: string;
    difficulty: string;
    isSkipped: boolean;
    hasErrors: boolean;
    details: any;
}

interface Props {
    results: TaskResult[];
}

export const BenchmarkResultsTable: React.FC<Props> = ({ results }) => {
    const [filters, setFilters] = useState({
        taskSource: '',
        taskName: '',
        language: '',
        domain: '',
        difficulty: ''
    });
    const [currentPage, setCurrentPage] = useState(0);
    const [selectedTask, setSelectedTask] = useState<TaskResult | null>(null);

    const filteredResults = results.filter(result => {
        return (!filters.taskSource || result.taskSourceName.toLowerCase().includes(filters.taskSource.toLowerCase())) &&
               (!filters.taskName || result.taskName.toLowerCase().includes(filters.taskName.toLowerCase())) &&
               (!filters.language || result.languages.some(l => l.toLowerCase().includes(filters.language.toLowerCase()))) &&
               (!filters.domain || result.domain.toLowerCase().includes(filters.domain.toLowerCase())) &&
               (!filters.difficulty || result.difficulty.toLowerCase().includes(filters.difficulty.toLowerCase()));
    });

    const pageSize = 100;
    const totalPages = Math.ceil(filteredResults.length / pageSize);
    const paginatedResults = filteredResults.slice(currentPage * pageSize, (currentPage + 1) * pageSize);

    return (
        <div className="benchmark-results">
            <div className="benchmark-results-filters">
                <InputGroup
                    placeholder="Filter by task source"
                    value={filters.taskSource}
                    onChange={(e) => setFilters({...filters, taskSource: e.target.value})}
                />
                <InputGroup
                    placeholder="Filter by task name"
                    value={filters.taskName}
                    onChange={(e) => setFilters({...filters, taskName: e.target.value})}
                />
                <InputGroup
                    placeholder="Filter by language"
                    value={filters.language}
                    onChange={(e) => setFilters({...filters, language: e.target.value})}
                />
                <InputGroup
                    placeholder="Filter by domain"
                    value={filters.domain}
                    onChange={(e) => setFilters({...filters, domain: e.target.value})}
                />
                <InputGroup
                    placeholder="Filter by difficulty"
                    value={filters.difficulty}
                    onChange={(e) => setFilters({...filters, difficulty: e.target.value})}
                />
            </div>

            <HTMLTable interactive striped className="benchmark-results-table">
                <thead>
                    <tr>
                        <th>Task Source</th>
                        <th>Task Name</th>
                        <th>Languages</th>
                        <th>Domain</th>
                        <th>Difficulty</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {paginatedResults.map((result, index) => (
                        <tr key={index}>
                            <td>{result.taskSourceName}</td>
                            <td>{result.taskName}</td>
                            <td>{result.languages.join(', ')}</td>
                            <td>{result.domain}</td>
                            <td>{result.difficulty}</td>
                            <td>
                                {result.isSkipped ? 'Skipped' :
                                 result.hasErrors ? 'Has Errors' : 'Completed'}
                            </td>
                            <td>
                                <Button
                                    icon="info-sign"
                                    onClick={() => setSelectedTask(result)}
                                >
                                    Details
                                </Button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </HTMLTable>

            <div className="benchmark-results-pagination">
                <Button
                    disabled={currentPage === 0}
                    onClick={() => setCurrentPage(p => p - 1)}
                    icon="chevron-left"
                >
                    Previous
                </Button>
                <span>Page {currentPage + 1} of {totalPages}</span>
                <Button
                    disabled={currentPage === totalPages - 1}
                    onClick={() => setCurrentPage(p => p + 1)}
                    icon="chevron-right"
                >
                    Next
                </Button>
            </div>

            {selectedTask && (
                <TaskDetailsModal
                    task={selectedTask}
                    isOpen={!!selectedTask}
                    onClose={() => setSelectedTask(null)}
                />
            )}
        </div>
    );
};
