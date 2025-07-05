package org.donutellko.modularbench;

import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskResults;

import java.util.List;

public interface LLMClient {
    List<String> getAvailableLLMs();

    TaskResults.LlmGenerationResult generateSolution(ExecutionConfig executionConfig, String llmName, String prompt, String language);
}
