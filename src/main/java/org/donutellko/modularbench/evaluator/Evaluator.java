package org.donutellko.modularbench.evaluator;

import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskResults;
import org.donutellko.modularbench.dto.TaskSource;

import java.util.List;

public interface Evaluator {
    boolean matches(ExecutionConfig config, TaskSource.TaskDefinition task);

    List<TaskResults.TestExecutionResult> execute(TaskSource.TaskDefinition taskDefinition, TaskResults.LlmGenerationResult llmResponse);
}
