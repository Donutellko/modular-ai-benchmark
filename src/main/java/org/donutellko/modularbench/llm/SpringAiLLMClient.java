package org.donutellko.modularbench.llm;

import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskResults;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
public class SpringAiLLMClient implements LLMClient {

    @Autowired(required = false)
    private Map<String, ChatClient> chatClients;

    @Override
    public List<String> getAvailableLLMs() {
        if (chatClients == null) return Collections.emptyList();
        return chatClients.keySet().stream().toList();
    }

    @Override
    public TaskResults.LlmGenerationResult generateSolution(ExecutionConfig executionConfig, String llmName, String prompt, String language) {
        if (chatClients == null || !chatClients.containsKey(llmName)) {
            throw new IllegalArgumentException("LLM not found: " + llmName);
        }
        ChatClient client = chatClients.get(llmName);
        UserMessage userMessage = new UserMessage(prompt);

        ChatOptions.Builder chatOptions = ChatOptions.builder();
        if (executionConfig.getMaxTokens() != null && executionConfig.getMaxTokens() > 0) {
            chatOptions.maxTokens(executionConfig.getMaxTokens());
        }

        Prompt springPrompt = new Prompt(List.of(userMessage), chatOptions.build());

        ChatClient.CallResponseSpec response = client.prompt(springPrompt).call();
        String responseText = response.content();
        ChatResponseMetadata metadata = response.chatResponse().getMetadata();

        return TaskResults.LlmGenerationResult.builder()
                .modelName(llmName)
                .language(language)
                .prompt(prompt)
                .responseText(responseText)
                .responseCode("")
                .tokenCount(metadata.getUsage().getTotalTokens())
                .promptTokenCount(metadata.getUsage().getPromptTokens())
                .timeMillis(0) // TODO: Implement timing if needed
                .build();
    }
}
