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
    Map<TaskSource, Map<TaskSource.TaskDefinition, List<TaskResults>>> taskResultsMap;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (taskResultsMap != null) {
            for (Map.Entry<TaskSource, Map<TaskSource.TaskDefinition, List<TaskResults>>> sourceEntry : taskResultsMap.entrySet()) {
                TaskSource source = sourceEntry.getKey();
                sb.append(source.getName()).append(": ").append(source.getPath()).append("\n");
                Map<TaskSource.TaskDefinition, List<TaskResults>> defMap = sourceEntry.getValue();
                if (defMap != null) {
                    for (Map.Entry<TaskSource.TaskDefinition, List<TaskResults>> defEntry : defMap.entrySet()) {
                        TaskSource.TaskDefinition def = defEntry.getKey();
                        sb.append("  ").append(def.getName()).append(":\n");
                        List<TaskResults> results = defEntry.getValue();
                        if (results != null) {
                            for (TaskResults result : results) {
                                if (!result.getSkipReasons().isEmpty()) {
                                    sb.append("    - Skipped: ").append(String.join(", ", result.getSkipReasons())).append("\n");
                                } else {
                                    sb.append("    - Name: ").append(result.getLlmName()).append("\n");
                                    sb.append("    - Language: ").append(result.getLanguage()).append("\n");
                                    List<TaskResults.TestExecutionResult> testExecutionResult = result.getCodeExecutorResult();
                                    if (testExecutionResult != null && !testExecutionResult.isEmpty()) {
                                        sb.append("    - Code Executor Results:\n");
                                        for (TaskResults.TestExecutionResult codeResult : testExecutionResult) {
                                            sb.append("        - Time: ").append(codeResult.getTimeMillis()).append(" ms\n");
                                            sb.append("        - Output: ").append(codeResult.getOutput()).append("\n");
                                            sb.append("        - Error: ").append(codeResult.getError()).append("\n");
                                        }
                                    }
                                    List<TaskResults.LlmResponseEvaluationsResult> additionalResults = result.getAdditionalEvaluationResult();
                                    if (additionalResults != null && !additionalResults.isEmpty()) {
                                        sb.append("    - Additional Evaluations:\n");
                                        for (TaskResults.LlmResponseEvaluationsResult evalResult : additionalResults) {
                                            sb.append("      - Evaluation ").append(evalResult.getEvaluationNumber())
                                                    .append(": Score = ").append(evalResult.getScore()).append("\n");
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
