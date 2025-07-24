package org.donutellko.modularbench.evaluator;

import org.donutellko.modularbench.dto.TaskResults;
import org.donutellko.modularbench.dto.TaskSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PmdEvaluatorTest {

    @Test
    void testExecuteWhenGoodCode() {
        PmdEvaluator evaluator = new PmdEvaluator();
        TaskSource.TaskDefinition taskDefinition = TaskSource.TaskDefinition.builder()
                .name("test-task")
                .availableCriteria(List.of("java-pmd"))
                .build();

        TaskResults.LlmGenerationResult llmResponse = TaskResults.LlmGenerationResult.builder()
                .responseText("""
                        package test.pmd;
                        /**
                        * This is an example good code for Pmd to be happy.
                        */
                        public class Main {
                          /**
                          * Javadoc for everything.
                          */
                          public void method() {
                            System.out.println("Hello, PMD!");
                          }
                        }
                        """)
                .language("java")
                .build();

        List<TaskResults.LlmResponseEvaluationsResult> execute = evaluator.execute(taskDefinition, llmResponse);

        assertThat(execute).hasSize(1);
        TaskResults.LlmResponseEvaluationsResult result = execute.get(0);
        assertTrue(1.0 == result.getScore());
        assertThat(result.getCriteria()).isEqualTo("java-pmd");
        assertThat(result.getUnit()).isEqualTo("1/errors");
        assertThat(result.getOutput()).contains("High: 0,");
//        assertThat(result.getOutput()).contains("<pmd");
    }

    @Test
    void testExecuteWhenBadCode() {
        PmdEvaluator evaluator = new PmdEvaluator();
        TaskSource.TaskDefinition taskDefinition = TaskSource.TaskDefinition.builder()
                .name("test-task")
                .availableCriteria(List.of("java-pmd"))
                .build();

        TaskResults.LlmGenerationResult llmResponse = TaskResults.LlmGenerationResult.builder()
                .responseText("public class Test { public void /*???*/                    Method() { ;;;;; } ;;;; }")
                .language("java")
                .build();

        List<TaskResults.LlmResponseEvaluationsResult> execute = evaluator.execute(taskDefinition, llmResponse);

        assertThat(execute).hasSize(1);
        TaskResults.LlmResponseEvaluationsResult result = execute.get(0);
        assertTrue(0.1 < result.getScore() && result.getScore() < 0.4, "Actual: " + result.getScore());
        assertThat(result.getCriteria()).isEqualTo("java-pmd");
        assertThat(result.getUnit()).isEqualTo("1/errors");
        assertThat(result.getOutput()).contains("High: 1,");
        assertThat(result.getOutput()).contains("Unnecessary semicolon");
    }
}
