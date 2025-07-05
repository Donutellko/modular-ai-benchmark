package org.donutellko.modularbench;

import org.springframework.stereotype.Repository;

@Repository
public class LLMClientRegistry {
    public LLMClient getDefault() {
        // Return a Spring AI-based implementation
        return new SpringAiLLMClient();
    }
}
