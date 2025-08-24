package org.donutellko.modularbench.evaluator;

import freemarker.template.Template;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskResults;
import org.donutellko.modularbench.dto.TaskSource;
import org.donutellko.modularbench.llm.LLMClient;
import org.donutellko.modularbench.llm.LLMClientRegistry;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class LlmJudgeEvaluator implements Evaluator {
    private final LLMClientRegistry llmClientRegistry;
    private final freemarker.template.Configuration freemarkerConfig;
    private final String CRITERIA_NAME = "llm-judge-code-quality";

    @Override
    public boolean matches(ExecutionConfig config, TaskSource.TaskDefinition task) {
        return task.getLlmJudgePrompt() != null
                &&
                config.getCriteria().stream()
                        .filter(criteria -> CRITERIA_NAME.equalsIgnoreCase(criteria.getName()))
                        .anyMatch(ExecutionConfig.ExecutionParameter::getEnabled);
    }

    @SneakyThrows
    @Override
    public List<TaskResults.LlmResponseEvaluationsResult> execute(ExecutionConfig config, TaskSource.TaskDefinition taskDefinition, TaskResults.LlmGenerationResult llmResponse) {
        long startTime = System.nanoTime();

        TaskResults.CodeQualityResult.CodeQualityResultBuilder<?, ?> builder = TaskResults.CodeQualityResult.builder()
                .criteria(CRITERIA_NAME)
                .executorClass(LlmJudgeEvaluator.class.getName());

        String modelName = config.getLlmJudge();
        LLMClient llmClient;
        try {
            llmClient = llmClientRegistry.getForModel(modelName);
        } catch (IllegalArgumentException e) {
            return List.of(
                    builder.score(-1.0)
                            .unit("1/errors")
                            .timeMillis(-1.0)
                            .output("Could not find model " + modelName + ": " + e.getMessage())
                            .build()
            );
        }

        Map<String, Object> templateModel = new HashMap();
        templateModel.putAll(Map.<String, Object>of(
                "prompt", llmResponse.getPrompt(),
                "solution", llmResponse.getResponseText(),
                "language", llmResponse.getLanguage()
        ));

        Template promptTemplate = new Template("judgePrompt", new StringReader(taskDefinition.getLlmJudgePrompt()), freemarkerConfig);
        String judgePrompt = FreeMarkerTemplateUtils.processTemplateIntoString(promptTemplate, templateModel);

        TaskResults.LlmGenerationResult judgeResponse = llmClient.generateSolution(config, modelName, judgePrompt, llmResponse.getLanguage());

        Matcher matcher = Pattern.compile("\\d+[,.]?\\d*")
                .matcher(judgeResponse.getResponseText());

        Double score = null;
        if (matcher.find()) {
            String number = matcher.group().replace(',', '.');
            score = Double.parseDouble(number);
        } else {
            return List.of(builder.score(-1.0)
                    .unit("1/errors")
                    .timeMillis(-1.0)
                    .output("Could not find score in judge response " + judgeResponse.getResponseText())
                    .build());
        }

        double endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1_000_000.0; // ms

        return List.of(
                builder.score(score)
                        .unit("1")
                        .timeMillis(executionTime)
                        .output(judgeResponse.getResponseText())
                        .build()
        );
    }
}
