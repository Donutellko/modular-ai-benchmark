package org.donutellko.modularbench.evaluator;

import org.donutellko.modularbench.dto.TaskResults;
import org.donutellko.modularbench.dto.TaskSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CheckstyleEvaluatorTest {

    @Test
    void testExecuteWhenGoodCode() {
        CheckstyleEvaluator evaluator = new CheckstyleEvaluator();
        TaskSource.TaskDefinition taskDefinition = TaskSource.TaskDefinition.builder()
                .name("test-task")
                .availableCriteria(List.of("java-checkstyle"))
                .build();

        TaskResults.LlmGenerationResult llmResponse = TaskResults.LlmGenerationResult.builder()
                .responseText("public class Test { public void method() {} }")
                .language("java")
                .build();

        List<TaskResults.LlmResponseEvaluationsResult> execute = evaluator.execute(taskDefinition, llmResponse);

        assertThat(execute).hasSize(1);
        assertThat(execute.get(0))
                .isInstanceOf(TaskResults.CodeQualityResult.class)
                .extracting("criteria", "score", "unit")
                .containsExactly("java-checkstyle", 1.0, "1/errors");
    }

    @Test
    void testExecuteWhenBadCode() {
        CheckstyleEvaluator evaluator = new CheckstyleEvaluator();
        TaskSource.TaskDefinition taskDefinition = TaskSource.TaskDefinition.builder()
                .name("test-task")
                .availableCriteria(List.of("java-checkstyle"))
                .build();

        TaskResults.LlmGenerationResult llmResponse = TaskResults.LlmGenerationResult.builder()
                .responseText("public class Test { public void                    Method() { ;;;;; } ;;;; }")
                .language("java")
                .build();

        List<TaskResults.LlmResponseEvaluationsResult> execute = evaluator.execute(taskDefinition, llmResponse);

        assertThat(execute).hasSize(1);
        assertThat(execute.get(0))
                .isInstanceOf(TaskResults.CodeQualityResult.class)
                .extracting("criteria", "score", "unit")
                .containsExactly("java-checkstyle", 0.5, "1/errors");
    }
}