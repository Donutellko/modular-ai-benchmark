package org.donutellko.modularbench.evaluator;

import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskResults;
import org.donutellko.modularbench.dto.TaskSource;
import org.donutellko.modularbench.evaluator.codeexecutor.CodeExecutionResult;
import org.donutellko.modularbench.evaluator.codeexecutor.CodeExecutor;
import org.donutellko.modularbench.evaluator.codeexecutor.CodeExecutorRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnitTestExecutor implements Evaluator {

    public List<String> getSupportedCriteria() {
        return List.of("unit-test", "cpu-usage", "ram-usage");
    }

    @Override
    public boolean matches(ExecutionConfig config, TaskSource.TaskDefinition task) {
        return config.getCriteria().stream()
                .filter(criteria -> "unit-test".equalsIgnoreCase(criteria.getName()))
                .anyMatch(ExecutionConfig.ExecutionParameter::getEnabled);
    }

    @Override
    public List<TaskResults.LlmResponseEvaluationsResult> execute(TaskSource.TaskDefinition taskDefinition, TaskResults.LlmGenerationResult llmResponse) {
        String language = llmResponse.getLanguage();

        TaskSource.LanguageSpecificTask languageSpecificTask = taskDefinition.getTask().getLanguagesSpecific().get(language);

        List<TaskSource.TestDefinition> tests = new ArrayList<>();
        tests.addAll(languageSpecificTask.getPublicTests());
        tests.addAll(languageSpecificTask.getHiddenTests());

        CodeExecutor codeExecutor = CodeExecutorRegistry.getExecutor(language);

        Map<TaskSource.TestDefinition, TaskResults.LlmResponseEvaluationsResult> results = new HashMap<>();
        for (TaskSource.TestDefinition test: tests) {
            ;
            try {
                CodeExecutionResult execute = codeExecutor.execute(llmResponse.getResponseCode(), test.getCode());
                results.put(test, TaskResults.TestExecutionResult.builder()
                        .executorClass(codeExecutor.getClass().getName())
                        .score(execute.getExitCode() == 0 ? 1 : 0)
                        .unit("success")
                        .output(execute.getOutput())
                        .timeMillis(execute.getExecutionTime())
                        .exitCode(execute.getExitCode())
                        .build());

                results.put(test, TaskResults.MetricExecutionResult.builder()
                        .executorClass(codeExecutor.getClass().getName())
                        .criteria("cpu-usage")
                        .score(execute.getSolutionTime())
                        .unit("ms")
                        .timeMillis(execute.getExecutionTime())
                        .build());

                results.put(test, TaskResults.MetricExecutionResult.builder()
                        .executorClass(codeExecutor.getClass().getName())
                        .criteria("ram-usage")
                        .score(execute.getMemoryUsage())
                        .unit("Kb")
                        .build());

            } catch (Exception e) {
                TaskResults.TestExecutionResult executeResult = TaskResults.TestExecutionResult.builder()
                        .executorClass(codeExecutor.getClass().getName())
                        .error("Error executing test: " + e.getMessage())
                        .score(0)
                        .build();
                results.put(test, executeResult);
            }
        }

        return results.values().stream().toList();
    }
}
