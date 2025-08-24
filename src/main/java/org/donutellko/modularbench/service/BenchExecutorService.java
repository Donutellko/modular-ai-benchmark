package org.donutellko.modularbench.service;

import freemarker.template.Template;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.donutellko.modularbench.dto.BenchResults;
import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskResults;
import org.donutellko.modularbench.dto.TaskSource;
import org.donutellko.modularbench.dto.TaskSource.TaskDefinition;
import org.donutellko.modularbench.dto.TaskSource.TaskDescription;
import org.donutellko.modularbench.evaluator.Evaluator;
import org.donutellko.modularbench.evaluator.EvaluatorRegistry;
import org.donutellko.modularbench.llm.LLMClient;
import org.donutellko.modularbench.llm.LLMClientRegistry;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.StringReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BenchExecutorService {
    private static final boolean FILTER_BY_PARAMETERS = false;
    private static final boolean FILTER_BY_CRITERIA = true;
    private final LLMClientRegistry llmClientRegistry;
    private final EvaluatorRegistry evaluatorRegistry;
    private final freemarker.template.Configuration freemarkerConfig;
    private final BenchmarkProgressTracker progressTracker;

    public BenchResults evaluate(ExecutionConfig config, Iterable<TaskSource> taskSourceList, String resultFilename) {
        BenchmarkProgressTracker.BenchmarkStatus benchmarkStatus =
                progressTracker.createStatus(config, taskSourceList, resultFilename);

        try {
            Map<TaskSource, Map<TaskDefinition, TaskResults>> results = new HashMap<>();

            for (TaskSource taskSource : taskSourceList) {
                BenchmarkProgressTracker.TaskSourceStatus taskSourceStatus = benchmarkStatus.getTaskSourceStatus(taskSource);
                Map<TaskDefinition, TaskResults> taskResults = evaluateTaskSource(config, taskSource, taskSourceStatus);
                results.put(taskSource, taskResults);
            }

            progressTracker.completeRun(resultFilename);
            return BenchResults.builder()
                    .taskResultsMap(results)
                    .build();
        } catch (Exception e) {
            progressTracker.completeRun(resultFilename);
            throw new RuntimeException("Failed to evaluate benchmark", e);
        }
    }

    private Map<TaskDefinition, TaskResults> evaluateTaskSource(ExecutionConfig config, TaskSource taskSource,
                                                                BenchmarkProgressTracker.TaskSourceStatus status) {
        Map<TaskDefinition, TaskResults> taskSourceResults = new HashMap<>();

        for (TaskDefinition task : taskSource.getTasks()) {
            TaskResults taskResultsList = evaluateTask(taskSource, task, config);
            taskSourceResults.put(task, taskResultsList);

            int errorsCount = taskResultsList.getTaskResults().stream()
                    .mapToInt(tr -> Math.toIntExact(tr.getEvaluationResult().stream()
                            .filter(er -> er.getError() != null).count()))
                    .sum();

            // Update progress based on task results
            if (!taskResultsList.getSkipReasons().isEmpty()) {
                status.incrementFilteredOut();
            } else if (errorsCount > 0) {
                status.incrementError();
            } else {
                status.incrementCompleted();
            }
        }
        return taskSourceResults;
    }

    @SneakyThrows
    private TaskResults evaluateTask(TaskSource taskSource, TaskDefinition task, ExecutionConfig config) {
        TaskResults taskResults = TaskResults.builder()
                .taskSourceName(taskSource.getName())
                .taskSourcePath(taskSource.getPath())
                .taskDefinitionName(task.getName())
                .build();

        List<String> skipReasons = filter(config, task);

        if (!skipReasons.isEmpty()) {
            taskResults.getSkipReasons().addAll(skipReasons);
            return taskResults;
        }

        // For each language in intersection of config and task
        for (String lang : task.getLanguages()) {
            if (config.getLanguages() != null && !config.getLanguages().isEmpty()
                    && !config.getLanguages().contains(lang)) {
                continue;
            }
            // Get prompt for this language
            TaskDescription desc = task.getTask();
            TaskSource.LanguageSpecificTask descLang = desc.getLanguagesSpecific().getOrDefault(lang, null);

            String promptTemplateStr = descLang != null ? descLang.getDescription() : desc.getCommonPrompt();

            Map<String, Boolean> parameters = task.getAvailableParameters().stream()
                    .collect(Collectors.toMap(Function.identity(), p -> getExecParam(config, p)));

            Map<String, Object> templateModel = new HashMap();
            templateModel.putAll(Map.<String, Object>of(
                    "common_prompt", desc.getCommonPrompt(),
                    "language", lang,
                    "public_tests", descLang == null ? null : stringifyTests(descLang.getPublicTests()),
                    "hidden_tests", descLang == null ? null : stringifyTests(descLang.getHiddenTests()),
                    "parameters", parameters
            ));

            Template promptTemplate = new Template("taskPrompt", new StringReader(promptTemplateStr), freemarkerConfig);
            String promptFirstRun = FreeMarkerTemplateUtils.processTemplateIntoString(promptTemplate, templateModel);
            // Using second run in case the first used a "common_prompt" that in turn uses a template variable
            promptTemplate = new Template("taskPrompt", new StringReader(promptFirstRun), freemarkerConfig);
            String prompt = FreeMarkerTemplateUtils.processTemplateIntoString(promptTemplate, templateModel);

            // For each LLM, filter by config.llms if specified
            for (String modelName : config.getLlms()) {
                LLMClient llmClient;
                try {
                    llmClient = llmClientRegistry.getForModel(modelName);
                } catch (IllegalArgumentException e) {
                    taskResults.getSkipReasons().add(e.getMessage());
                    continue;
                }

                TaskResults.TaskResult taskResult = TaskResults.TaskResult.builder()
                        .providerName(llmClient.getClass().getName())
                        .modelName(modelName)
                        .language(lang)
                        .build();

                TaskResults.LlmGenerationResult llmResponse = llmClient.generateSolution(config, modelName, prompt, lang);
                taskResult.setLlmResponse(llmResponse);

                List<Evaluator> evaluators = evaluatorRegistry.getEvaluators(config, task);
                for (Evaluator evaluator : evaluators) {
                    List<TaskResults.LlmResponseEvaluationsResult> evaluationResult = evaluator.execute(config, task, llmResponse);
                    taskResult.getEvaluationResult().addAll(evaluationResult);
                }

                taskResults.getTaskResults().add(taskResult);
            }
        }
        return taskResults;
    }

    private Object stringifyTests(List<TaskSource.TestDefinition> tests) {
        return tests.stream()
                .map(TaskSource.TestDefinition::getCode)
                .collect(Collectors.joining("\n"));
    }

    private Boolean getExecParam(ExecutionConfig config, String paramName) {
        return config.getParameters().stream()
                .filter(ep -> paramName.equalsIgnoreCase(ep.getName()))
                .findFirst()
                .map(ExecutionConfig.ExecutionParameter::getEnabled)
                .orElse(null);
    }

    /**
     * returns skip reasons if the task should be skipped, otherwise returns empty list
     */
    private static List<String> filter(ExecutionConfig config, TaskDefinition task) {
        List<String> skipReasons = new ArrayList<>();

        // Filter by difficulty
        if (config.getDifficulties() != null && !config.getDifficulties().isEmpty()
                && !config.getDifficulties().contains(task.getDifficulty())) {
            skipReasons.add(task.getDifficulty() + " not in config difficulties: " + config.getDifficulties());
        }

        // Filter by area
        if (config.getAreas() != null && !config.getAreas().isEmpty()
                && (task.getArea() == null || !config.getAreas().contains(task.getArea()))) {
            skipReasons.add("Area " + task.getArea() + " not in config areas: " + config.getAreas());
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
            if (!found) {
                skipReasons.add("No matching language in task " + task.getName() + ": " + taskLangs
                        + " for config languages: " + configLangs);
            }
        }
        if (FILTER_BY_PARAMETERS) {
            // If we want to only run tasks that have the used parameter
            if (config.getParameters() != null && !config.getParameters().isEmpty()) {
                boolean allMatch = config.getParameters().stream().allMatch(param ->
                        task.getAvailableParameters().contains(param.getName())
                );
                if (!allMatch) {
                    skipReasons.add("Not all parameters in config are available in task: "
                            + task.getAvailableParameters() + " for config parameters: " + config.getParameters());
                }
            }
        }
        if (FILTER_BY_CRITERIA) {
            // We want all enabled criteria to be available for the task
            if (config.getCriteria() != null && !config.getCriteria().isEmpty()) {
                boolean allMatch = config.getCriteria()
                        .stream().filter(ExecutionConfig.ExecutionParameter::getEnabled)
                        .allMatch(param -> task.getAvailableCriteria().contains(param.getName()));
                if (!allMatch) {
                    List<String> requiredCriteria = config.getCriteria()
                            .stream().filter(ExecutionConfig.ExecutionParameter::getEnabled)
                            .map(ExecutionConfig.ExecutionParameter::getName).toList();
                    Set<String> diffSet = new HashSet<>(requiredCriteria);
                    diffSet.removeAll(task.getAvailableCriteria());

                    skipReasons.add("Criteria mismatch: " + diffSet
                            + ", available in task: " + task.getAvailableCriteria()
                            + ", required in config: " + requiredCriteria);
                }
            }
        }

        return skipReasons;
    }
}
