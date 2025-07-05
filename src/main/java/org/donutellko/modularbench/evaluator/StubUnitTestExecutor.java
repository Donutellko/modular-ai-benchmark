package org.donutellko.modularbench.evaluator;

import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskResults;
import org.donutellko.modularbench.dto.TaskSource;

import java.util.ArrayList;
import java.util.List;

public class StubUnitTestExecutor implements Evaluator {

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
    public List<TaskResults.TestExecutionResult> execute(TaskSource.TaskDefinition taskDefinition, TaskResults.LlmGenerationResult llmResponse) {
        // Stub: print or log the result
        String language = llmResponse.getLanguage();
        String output = ("Executing code for: " + taskDefinition.getName() +
                ", LLM: " + llmResponse.getModelName() +
                ", Language: " + language);

        TaskSource.LanguageSpecificTask languageSpecificTask = taskDefinition.getTask().getLanguagesSpecific().get(language);

        List<TaskSource.TestDefinition> tests = new ArrayList<>();
        tests.addAll(languageSpecificTask.getPublicTests());
        tests.addAll(languageSpecificTask.getHiddenTests());

        for (TaskSource.TestDefinition test: tests) {

        }
        return List.of();
    }
}
