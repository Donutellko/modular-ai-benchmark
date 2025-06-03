package org.donutellko.modularbench;

public class LLMClientRegistry {
    public static LLMClient getDefault() {
        // Return a Spring AI-based implementation
        return new SpringAiLLMClient();
    }
}
