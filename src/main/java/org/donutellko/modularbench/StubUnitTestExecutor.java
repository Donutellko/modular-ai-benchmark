package org.donutellko.modularbench;

import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskResults;
import org.donutellko.modularbench.dto.TaskSource;
import org.donutellko.modularbench.evaluator.Evaluator;

import java.util.List;

public class StubUnitTestExecutor implements Evaluator {

    @Override
    public boolean matches(ExecutionConfig config, TaskSource.TaskDefinition task) {
        return config.getCriteria().stream()
                .filter(criteria -> "unit_test".equalsIgnoreCase(criteria.getName()))
                .anyMatch(ExecutionConfig.ExecutionParameter::getEnabled);
    }

    @Override
    public List<TaskResults.TestExecutionResult> execute(TaskSource.TaskDefinition taskDefinition, TaskResults.LlmGenerationResult llmResponse) {
        // Stub: print or log the result
        System.out.println("Executing code for: " + taskDefinition.getName() +
                ", LLM: " + llmResponse.getLlmName() +
                ", Language: " + taskDefinition.getLanguages());
        // TODO
        return List.of();
    }
}
