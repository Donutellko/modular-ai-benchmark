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

    public Object toObject() {
        return taskResultsMap.entrySet().stream().map(sources -> Map.of(
            "task-source-name", sources.getKey().getName(),
            "task-source-path", sources.getKey().getPath(),

            "results", sources.getValue().entrySet().stream().map(tasks -> Map.of(
                "name", tasks.getKey().getName(),
                "area", tasks.getKey().getArea(),
                "details", tasks.getValue()
            )).toList()
        )).toList();
    }

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
                                        for (TaskResults.LlmResponseEvaluationsResult eval : testExecutionResult) {
                                            sb.append("      - ").append(eval.getCriteria()).append(": Score = ").append(eval.getScore()).append("\t\t\t").append(eval.getExecutionId()).append("\n");
                                            if (eval instanceof TaskResults.TestExecutionResult test) {
                                                //
                                            } else {
                                                //
                                            }
                                            if (eval.getError() != null && !eval.getError().isEmpty()) {
                                                sb.append("        - error: ").append(eval.getError()).append("\n");
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
