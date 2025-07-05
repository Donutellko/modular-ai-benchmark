package org.donutellko.modularbench.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor
@Builder
public class BenchResults {
    private final Map<TaskSource, Map<TaskSource.TaskDefinition, TaskResults>> taskResultsMap;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (taskResultsMap != null) {
            for (Map.Entry<TaskSource, Map<TaskSource.TaskDefinition, TaskResults>> sourceEntry : taskResultsMap.entrySet()) {
                TaskSource source = sourceEntry.getKey();
                sb.append(source.getName()).append(": ").append(source.getPath()).append("\n");
                Map<TaskSource.TaskDefinition, TaskResults> defMap = sourceEntry.getValue();
                if (defMap != null) {
                    for (Map.Entry<TaskSource.TaskDefinition, TaskResults> defEntry : defMap.entrySet()) {
                        TaskSource.TaskDefinition def = defEntry.getKey();
                        sb.append("  ").append(def.getName()).append(":\n");
                        TaskResults results = defEntry.getValue();
                        if (results != null) {
                            if (!results.getSkipReasons().isEmpty()) {
                                sb.append("    - Skipped: ").append(String.join(", ", results.getSkipReasons())).append("\n");
                            } else {
                                for (TaskResults.TaskResult result : results.getTaskResults()) {
                                    sb.append("    - Name: ").append(result.getModelName()).append("\n");
                                    sb.append("    - Language: ").append(result.getLanguage()).append("\n");
                                    List<TaskResults.LlmResponseEvaluationsResult> testExecutionResult = result.getEvaluationResult();
                                    if (testExecutionResult != null && !testExecutionResult.isEmpty()) {
                                        sb.append("    - Code Executor Results:\n");
                                        for (TaskResults.LlmResponseEvaluationsResult evaluationResult : testExecutionResult) {
                                            if (evaluationResult instanceof TaskResults.TestExecutionResult testResult) {
                                                sb.append("      - Test ").append(testResult.getEvaluationNumber())
                                                        .append(": Score = ").append(testResult.getScore()).append("\n");
                                                sb.append("        - Type: ").append(testResult.getCriteria()).append("\n");
                                            } else {
                                                sb.append("      - Evaluation ").append(evaluationResult.getEvaluationNumber())
                                                        .append(": Score = ").append(evaluationResult.getScore()).append("\n");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return sb.toString();
    }
}
