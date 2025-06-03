package org.donutellko.modularbench;

import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskSource;
import org.donutellko.modularbench.dto.TaskSource.TaskDefinition;
import org.donutellko.modularbench.dto.TaskSource.TaskDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BenchExecutor {

    public static void evaluate(ExecutionConfig config, Iterable<TaskSource> taskSourceList) {
        List<TaskResult> results = new ArrayList<>();
        LLMClient llmClient = LLMClientRegistry.getDefault(); // Placeholder for LLM client registry
        CodeExecutor codeExecutor = CodeExecutorRegistry.getDefault(); // Placeholder for CodeExecutor registry

        for (TaskSource taskSource : taskSourceList) {
            for (TaskDefinition task : taskSource.getTasks()) {
                // Filter by difficulty
                if (config.getDifficulties() != null && !config.getDifficulties().isEmpty()
                        && !config.getDifficulties().contains(task.getDifficulty())) {
                    continue;
                }
                // Filter by area
                if (config.getAreas() != null && !config.getAreas().isEmpty()
                        && (task.getArea() == null || !config.getAreas().contains(task.getArea()))) {
                    continue;
                }
                // Filter by language
                Set<String> configLangs = config.getLanguages();
                List<String> taskLangs = task.getLanguages();
                if (configLangs != null && !configLangs.isEmpty()) {
                    boolean found = false;
                    for (String lang : taskLangs) {
                        if (configLangs.contains(lang)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) continue;
                }
                // Filter by parameters
                if (config.getParameters() != null && !config.getParameters().isEmpty()) {
                    boolean allMatch = config.getParameters().stream().allMatch(param ->
                        task.getAvailableParameters().contains(param.getName()) == Boolean.TRUE.equals(param.getEnabled())
                    );
                    if (!allMatch) continue;
                }
                // Filter by criteria
                if (config.getCriteria() != null && !config.getCriteria().isEmpty()) {
                    boolean allMatch = config.getCriteria().stream().allMatch(param ->
                        task.getAvailableCriteria().contains(param.getName()) == Boolean.TRUE.equals(param.getEnabled())
                    );
                    if (!allMatch) continue;
                }

                // For each language in intersection of config and task
                for (String lang : task.getLanguages()) {
                    if (config.getLanguages() != null && !config.getLanguages().isEmpty()
                            && !config.getLanguages().contains(lang)) {
                        continue;
                    }
                    // Get prompt for this language
                    TaskDescription desc = task.getTask();
                    String prompt = desc.getLanguagesSpecific() != null && desc.getLanguagesSpecific().containsKey(lang)
                            ? desc.getLanguagesSpecific().get(lang).getDescription()
                            : desc.getCommonPrompt();

                    // For each LLM, filter by config.llms if specified
                    for (String llmName : llmClient.getAvailableLLMs()) {
                        if (config.getLlms() != null && !config.getLlms().isEmpty()
                                && !config.getLlms().contains(llmName)) {
                            continue;
                        }
                        String llmResponse = llmClient.generateSolution(llmName, prompt, lang);
                        TaskResult result = new TaskResult(taskSource, task, config, lang, llmName, prompt, llmResponse);
                        results.add(result);
                        codeExecutor.execute(result);
                    }
                }
            }
        }
    }
}
