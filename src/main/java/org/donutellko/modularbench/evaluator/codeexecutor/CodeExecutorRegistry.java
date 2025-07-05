package org.donutellko.modularbench.evaluator.codeexecutor;

public class CodeExecutorRegistry {
    public static CodeExecutor getExecutor(String language) {
        switch (language.toLowerCase()) {
            case "java":
                return new JavaExecutor();
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
    }
}
