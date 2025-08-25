package org.donutellko.modularbench.evaluator;

import lombok.SneakyThrows;
import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskResults;
import org.donutellko.modularbench.dto.TaskSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PriceEvaluator implements Evaluator {

    @Override
    public boolean matches(ExecutionConfig config, TaskSource.TaskDefinition task) {
        return true;
    }

    @SneakyThrows
    @Override
    public List<TaskResults.LlmResponseEvaluationsResult> execute(ExecutionConfig config, TaskSource.TaskDefinition taskDefinition, TaskResults.LlmGenerationResult llmResponse) {
        long startTime = System.nanoTime();

        double endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1_000_000.0; // ms

        return List.of(
                TaskResults.CodeQualityResult.builder()
                        .criteria("token-count")
                        .score(llmResponse.getTokenCount() + 0.0)
                        .executorClass(PriceEvaluator.class.getName())
                        .unit("tokens")
                        .timeMillis(executionTime)
                        .output("Tokens used for output: " + llmResponse.getTokenCount()
                                + ", for input: " + llmResponse.getPromptTokenCount())
                        .build()
        );
    }
}
