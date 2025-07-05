package org.donutellko.modularbench.evaluator.codeexecutor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CodeExecutionResult {
    private final String output;
    private final String error;
    private final int exitCode;
    private final Double executionTime;
    private final Double solutionTime;
    private final Double memoryUsage;
    private final String preparedCode;
}
