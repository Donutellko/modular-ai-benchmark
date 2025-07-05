package org.donutellko.modularbench.llm;

import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskResults;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class StubLLMClient implements LLMClient {
    @Override
    public List<String> getAvailableLLMs() {
        return Collections.singletonList("stub-llm");
    }

    @Override
    public TaskResults.LlmGenerationResult generateSolution(ExecutionConfig executionConfig, String llmName, String prompt, String language) {
        return TaskResults.LlmGenerationResult.builder()
                .modelName("[stub] " + llmName)
                .prompt(prompt)
                .language(language)
                .responseText(
                        "// LLM stub response for " + llmName + " (" + language + "): " + prompt
                                + "public static int solution(int a, int b) { return 1; }")
                .responseCode("200")
                .tokenCount(0)
                .promptTokenCount(0)
                .timeMillis(0)
                .build();
    }
}
