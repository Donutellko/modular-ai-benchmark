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
    private final double executionTime;
    private final double cpuTime;
    private final double memoryUsage;
}
