package org.donutellko.modularbench.evaluator;

import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskResults;
import org.donutellko.modularbench.dto.TaskSource;
import org.donutellko.modularbench.evaluator.codeexecutor.CodeExecutionResult;
import org.donutellko.modularbench.evaluator.codeexecutor.CodeExecutor;
import org.donutellko.modularbench.evaluator.codeexecutor.CodeExecutorRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
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

        List<TaskResults.LlmResponseEvaluationsResult> results = new ArrayList<>();
        for (int i = 0; i < tests.size(); i++) {
            TaskSource.TestDefinition test = tests.get(i);
            String executionId = taskDefinition.getSource() + "/" + taskDefinition.getName() + "/" + language
                    + "/test-" + i + test.hashCode() + "/" + System.nanoTime();
            try {
                CodeExecutionResult execute = codeExecutor.execute(llmResponse.getResponseCode(), test.getCode());

                results.add(TaskResults.TestExecutionResult.builder()
                        .executionId(executionId)
                        .executorClass(codeExecutor.getClass().getName())
                        .score(execute.getExitCode() == 0 ? 1 : 0)
                        .unit("success")
                        .output(execute.getOutput())
                        .timeMillis(execute.getExecutionTime())
                        .exitCode(execute.getExitCode())
                        .preparedCode(execute.getPreparedCode())
                        .build());

                results.add(TaskResults.MetricExecutionResult.builder()
                        .executionId(executionId)
                        .executorClass(codeExecutor.getClass().getName())
                        .criteria("cpu-usage")
                        .score(execute.getSolutionTime())
                        .unit("ms")
                        .timeMillis(execute.getExecutionTime())
                        .build());

                results.add(TaskResults.MetricExecutionResult.builder()
                        .executionId(executionId)
                        .executorClass(codeExecutor.getClass().getName())
                        .criteria("ram-usage")
                        .score(execute.getMemoryUsage())
                        .unit("Kb")
                        .build());

            } catch (Exception e) {
                TaskResults.TestExecutionResult executeResult = TaskResults.TestExecutionResult.builder()
                        .executionId(executionId)
                        .executorClass(codeExecutor.getClass().getName())
                        .error("Error executing test: " + e.getMessage())
                        .score(0)
                        .build();
                results.add(executeResult);
            }
        }

        return results;
    }
}
