package org.donutellko.modularbench.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
@Builder
public class TaskResults {
    private String taskSourcePath;
    private String taskSourceName;
    private String taskDefinitionName;
    private List<String> skipReasons;
    @Builder.Default
    private List<TaskResult> taskResults = new ArrayList<>();

    @Data
    @RequiredArgsConstructor
    @Builder
    public static class TaskResult {
        private String language;
        private String llmName;
        private LlmGenerationResult llmResponse;
        private List<LlmResponseEvaluationsResult> evaluationResult;
    }

    @Data
    @RequiredArgsConstructor
    @Builder
    public static class LlmGenerationResult {
        private String llmName;
        private String prompt;
        private String responseText;
        private String responseCode;
        private int tokenCount;
        private int promptTokenCount;
        private long timeMillis;
    }

    @Data
    @RequiredArgsConstructor
    @Builder
    public static class LlmResponseEvaluationsResult {
        private int evaluationNumber;
        private double score;
        private String evaluationType;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @RequiredArgsConstructor
    @Builder
    public static class TestExecutionResult extends LlmResponseEvaluationsResult {
        @Builder.Default
        private String evaluationType = "unit-test";
        private boolean success;
        private String output;
        private String error;
        private int exitCode;
        private long timeMillis;

        public boolean isSuccess() {
            return success && exitCode == 0 && error == null;
        }
    }
}
