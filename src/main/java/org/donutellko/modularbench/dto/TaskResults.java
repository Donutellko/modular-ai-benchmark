package org.donutellko.modularbench.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class TaskResults {
    private String taskSourcePath;
    private String taskSourceName;
    private String taskDefinitionName;
    @Builder.Default
    private List<String> skipReasons = new ArrayList<>();
    @Builder.Default
    private List<TaskResult> taskResults = new ArrayList<>();

    @Data
    @Builder
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class TaskResult {
        private final String language;
        private final String providerName;
        private final String modelName;
        private LlmGenerationResult llmResponse;
        @Builder.Default
        private final List<LlmResponseEvaluationsResult> evaluationResult = new ArrayList<>();
    }

    @Data
    @Builder
    @RequiredArgsConstructor
    public static class LlmGenerationResult {
        private final String modelName;
        private final String prompt;
        private final String language;
        private final String responseText;
        private final String responseCode;
        private final int tokenCount;
        private final int promptTokenCount;
        private final long timeMillis;
    }

    @Data
    @SuperBuilder
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static abstract class LlmResponseEvaluationsResult {
        private String executorClass;
        private String executionId; // unique ID for the execution (shared between metrics calculated for a test run)
        private String criteria; // required
        private Double score;    // evaluation result, 0 or 1 for tests, value for metrics
        private String unit;     // units for score, e.g. "success" for tests, "ms" for time, "bytes" for memory, etc.
        private String output;   // if applicable
        private String error;    // if the evaluation failed
        private Double timeMillis; // time to execute the evaluation
    }

    @Data
    @SuperBuilder
    @AllArgsConstructor
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class TestExecutionResult extends LlmResponseEvaluationsResult {
        @Builder.Default
        private String criteria = "unit-test";
        private int testNumber; // number of unit test
        private int exitCode;
        private String preparedCode;
    }

    @Data
    @SuperBuilder
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class MetricExecutionResult extends LlmResponseEvaluationsResult {
    }

    @Data
    @SuperBuilder
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class CodeQualityResult extends LlmResponseEvaluationsResult {
    }
}
