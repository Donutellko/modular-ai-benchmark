package org.donutellko.modularbench;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SpringAiLLMClient implements LLMClient {

    @Autowired(required = false)
    private Map<String, ChatClient> chatClients;

    @Override
    public List<String> getAvailableLLMs() {
        if (chatClients == null) return Collections.emptyList();
        return chatClients.keySet().stream().toList();
    }

    @Override
    public String generateSolution(String llmName, String prompt, String language) {
        if (chatClients == null || !chatClients.containsKey(llmName)) {
            throw new IllegalArgumentException("LLM not found: " + llmName);
        }
        ChatClient client = chatClients.get(llmName);
        UserMessage userMessage = new UserMessage(prompt);
        Prompt springPrompt = new Prompt(List.of(userMessage));
        return client.prompt(springPrompt).call().chatResponse().getResult().getOutput().getText();
    }
}
