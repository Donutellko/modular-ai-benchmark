package org.donutellko.modularbench;

public class CodeExecutorRegistry {
    public static CodeExecutor getDefault() {
        // Return a stub or singleton instance
        return new StubCodeExecutor();
    }
}
