package org.donutellko.modularbench.evaluator;

import lombok.SneakyThrows;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.rule.RulePriority;
import net.sourceforge.pmd.reporting.GlobalAnalysisListener;
import net.sourceforge.pmd.reporting.Report;
import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskResults;
import org.donutellko.modularbench.dto.TaskSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PmdEvaluator implements Evaluator {

    @Override
    public boolean matches(ExecutionConfig config, TaskSource.TaskDefinition task) {
        return config.getLanguages().stream().anyMatch("java"::equalsIgnoreCase)
                &&
                config.getCriteria().stream()
                        .filter(criteria -> "java-pmd".equalsIgnoreCase(criteria.getName()))
                        .anyMatch(ExecutionConfig.ExecutionParameter::getEnabled);
    }

    @SneakyThrows
    @Override
    public List<TaskResults.LlmResponseEvaluationsResult> execute(TaskSource.TaskDefinition taskDefinition, TaskResults.LlmGenerationResult llmResponse) {
        long startTime = System.nanoTime();

        Path tempDir = Files.createTempDirectory("ai_benchmark_checkstyle-");
        Path tempFile = Files.createFile(Path.of(tempDir.toString(), "Main.java"));
        Files.writeString(tempFile, llmResponse.getResponseText());

        PMDConfiguration pmdConfiguration = new PMDConfiguration();
        pmdConfiguration.addInputPath(tempFile);
        pmdConfiguration.setRuleSets(List.of("pmd_ruleset_quickstart.xml"));

        PmdAnalysis pmdAnalysis = PmdAnalysis.create(pmdConfiguration);
        GlobalAnalysisListener counter = new GlobalAnalysisListener.ViolationCounterListener();
        pmdAnalysis.addListener(counter);

        Report report = pmdAnalysis.performAnalysisAndCollectReport();

        pmdAnalysis.close();
        double endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1_000_000.0; // ms

        Map<RulePriority, Long> counts = Arrays.stream(RulePriority.values())
                .collect(Collectors.toMap(Function.identity(), level ->
                        report.getViolations().stream()
                                .filter(violation -> violation.getRule().getPriority() == level)
                                .count()));

        String output = Arrays.stream(RulePriority.values())
                .map(priority -> priority.toString() + ": " + counts.getOrDefault(priority, 0L))
                .collect(Collectors.joining(", "));

        String reportString = report.getViolations().stream()
                .map(violation -> String.format("[%s] %s: %s at %s:%d",
                        violation.getRule().getPriority().toString(),
                        violation.getRule().getName(),
                        violation.getDescription(),
                        violation.getLocation().getFileId().getFileName(),
                        violation.getBeginLine()))
                .collect(Collectors.joining("\n"));

        return List.of(
                TaskResults.CodeQualityResult.builder()
                        .criteria("java-pmd")
                        .score(1.0 /
                                (1 + counts.getOrDefault(RulePriority.HIGH, 0L)
                                        + 0.5 * counts.getOrDefault(RulePriority.MEDIUM_HIGH, 0L)
                                        + 0.1 * counts.getOrDefault(RulePriority.MEDIUM, 0L)
                                        + 0.05 * counts.getOrDefault(RulePriority.MEDIUM_LOW, 0L)
                                        + 0.01 * counts.getOrDefault(RulePriority.LOW, 0L))
                        )
                        .executorClass(PmdEvaluator.class.getName())
                        .unit("1/errors")
                        .timeMillis(executionTime)
                        .output(output + "\n" + reportString)
                        .build()
        );
    }
}
