package org.donutellko.modularbench.evaluator.codeexecutor;

public interface CodeExecutor {
    CodeExecutionResult execute(String code, String testCode);
}
