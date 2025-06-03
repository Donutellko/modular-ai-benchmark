package org.donutellko.modularbench;

public class LLMClientRegistry {
    public static LLMClient getDefault() {
        // Return a stub or singleton instance
        return new StubLLMClient();
    }
}
