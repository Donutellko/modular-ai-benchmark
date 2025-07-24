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
                .responseText("""
                        /**
                        * This is an example good code for checkstyle to be happy.
                        */
                        public class Main {
                          /**
                          * Javadoc for everything.
                          */
                          public void method() {}
                        }
                        """)
                .language("java")
                .build();

        List<TaskResults.LlmResponseEvaluationsResult> execute = evaluator.execute(taskDefinition, llmResponse);

        assertThat(execute).hasSize(1);
        TaskResults.LlmResponseEvaluationsResult result = execute.get(0);
        assertTrue(1.0 == result.getScore());
        assertThat(result.getCriteria()).isEqualTo("java-checkstyle");
        assertThat(result.getUnit()).isEqualTo("1/errors");
        assertThat(result.getOutput()).contains("warning: 0,");
        assertThat(result.getOutput()).contains("<checkstyle");
    }

    @Test
    void testExecuteWhenBadCode() {
        CheckstyleEvaluator evaluator = new CheckstyleEvaluator();
        TaskSource.TaskDefinition taskDefinition = TaskSource.TaskDefinition.builder()
                .name("test-task")
                .availableCriteria(List.of("java-checkstyle"))
                .build();

        TaskResults.LlmGenerationResult llmResponse = TaskResults.LlmGenerationResult.builder()
                .responseText("public class Test { public void /*???*/                    Method() { ;;;;; } ;;;; }")
                .language("java")
                .build();

        List<TaskResults.LlmResponseEvaluationsResult> execute = evaluator.execute(taskDefinition, llmResponse);

        assertThat(execute).hasSize(1);
        TaskResults.LlmResponseEvaluationsResult result = execute.get(0);
        assertTrue(0.1 < result.getScore() && result.getScore() < 0.2);
        assertThat(result.getCriteria()).isEqualTo("java-checkstyle");
        assertThat(result.getUnit()).isEqualTo("1/errors");
        assertThat(result.getOutput()).contains("warning: 10,");
        assertThat(result.getOutput()).contains("should be alone on a line");
    }
}