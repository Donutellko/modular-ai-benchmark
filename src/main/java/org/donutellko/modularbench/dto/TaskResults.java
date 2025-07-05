package org.donutellko.modularbench.dto;

import lombok.*;

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
        private final List<LlmResponseEvaluationsResult> evaluationResult;
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
    public static abstract class LlmResponseEvaluationsResult {
        private String criteria;
        private int evaluationNumber;
        private double score;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class TestExecutionResult extends LlmResponseEvaluationsResult {
        @Builder.Default
        private String criteria = "unit-test";
        private String executorClass;
        private boolean success;
        private String output;
        private String error;
        private int exitCode;
        private long timeMillis;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class CodeQualityResult extends LlmResponseEvaluationsResult {
        private String output;
        private long timeMillis;
    }
}
