package org.donutellko.modularbench;

import java.util.List;

public interface LLMClient {
    List<String> getAvailableLLMs();
    String generateSolution(String llmName, String prompt, String language);
}
