package org.donutellko.modularbench;

import java.util.Collections;
import java.util.List;

public class StubLLMClient implements LLMClient {
    @Override
    public List<String> getAvailableLLMs() {
        return Collections.singletonList("stub-llm");
    }

    @Override
    public String generateSolution(String llmName, String prompt, String language) {
        return "// LLM response for " + llmName + " (" + language + "): " + prompt;
    }
}
