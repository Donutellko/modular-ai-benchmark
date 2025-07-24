package org.donutellko.modularbench.evaluator;

import com.puppycrawl.tools.checkstyle.Checker;
import lombok.SneakyThrows;
import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskResults;
import org.donutellko.modularbench.dto.TaskSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CheckstyleEvaluator implements Evaluator {

    @Override
    public boolean matches(ExecutionConfig config, TaskSource.TaskDefinition task) {
        return config.getLanguages().stream().anyMatch("java"::equalsIgnoreCase)
                &&
                config.getCriteria().stream()
                        .filter(criteria -> "java-checkstyle".equalsIgnoreCase(criteria.getName()))
                        .anyMatch(ExecutionConfig.ExecutionParameter::getEnabled);
    }

    @SneakyThrows
    @Override
    public List<TaskResults.LlmResponseEvaluationsResult> execute(TaskSource.TaskDefinition taskDefinition, TaskResults.LlmGenerationResult llmResponse) {
        long startTime = System.nanoTime();
        Checker checker = new Checker();
        checker.setSeverity("INFO");

        Path temp = Files.createTempFile("ai_benchmark_checkstyle-", ".java");
        Files.writeString(temp, llmResponse.getResponseText());

        // save code to tmp file
        int errors = checker.process(List.of(temp.toFile()));

        double endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1_000_000.0; // ms

        return List.of(
                TaskResults.CodeQualityResult.builder()
                        .criteria("java-checkstyle")
                        .score(1.0 / (1 + errors))
                        .executorClass(CheckstyleEvaluator.class.getName())
                        .unit("1/errors")
                        .timeMillis(executionTime)
                        .build()
        );
    }
}
