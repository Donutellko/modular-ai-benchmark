package org.donutellko.modularbench.llm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LLMClientRegistry {
    public final List<LLMClient> llmClients;

    public LLMClient getForModel(String modelName) {
        // Return a Spring AI-based implementation
        return llmClients.stream().filter(llmClient -> llmClient.getAvailableLLMs().contains(modelName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Provider not found for: " + modelName));
    }
}
