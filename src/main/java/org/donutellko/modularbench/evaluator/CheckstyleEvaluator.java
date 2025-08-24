package org.donutellko.modularbench.evaluator;

import com.puppycrawl.tools.checkstyle.*;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.api.SeverityLevelCounter;
import lombok.SneakyThrows;
import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskResults;
import org.donutellko.modularbench.dto.TaskSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CheckstyleEvaluator implements Evaluator {

    @Override
    public boolean matches(ExecutionConfig config, TaskSource.TaskDefinition task) {
        return config.getLanguages().stream().anyMatch("java"::equalsIgnoreCase)
                &&
                config.getCriteria().stream()
                        .filter(criteria -> "java-checkstyle".equalsIgnoreCase(criteria.getName()))
                        .anyMatch(ExecutionConfig.ExecutionParameter::getEnabled);
    }

    @SneakyThrows
    @Override
    public List<TaskResults.LlmResponseEvaluationsResult> execute(ExecutionConfig config, TaskSource.TaskDefinition taskDefinition, TaskResults.LlmGenerationResult llmResponse) {
        long startTime = System.nanoTime();

        Path tempDir = Files.createTempDirectory("ai_benchmark_checkstyle-");
        Path tempFile = Files.createFile(Path.of(tempDir.toString(), "Main.java"));
        Files.writeString(tempFile, llmResponse.getResponseText());

        // Load Checkstyle configuration (use Google style as example)
        File configFile = new File(getClass().getClassLoader().getResource("checkstyle_google_config.xml").getFile());
        Configuration checkerConfig = ConfigurationLoader.loadConfiguration(
                configFile.getAbsolutePath(),
                new PropertiesExpander(new Properties())
        );

        Checker checker = new Checker();
        checker.setModuleClassLoader(Checker.class.getClassLoader());
        checker.configure(checkerConfig);

        Map<SeverityLevel, SeverityLevelCounter> counters = Arrays.stream(SeverityLevel.values())
                .collect(Collectors.toMap(Function.identity(), SeverityLevelCounter::new));
        counters.values().forEach(checker::addListener);

        final OutputStream errorsTextStream = new ByteArrayOutputStream();
        AuditListener textListener = new XMLLogger(errorsTextStream, AbstractAutomaticBean.OutputStreamOptions.CLOSE);
        checker.addListener(textListener);

        checker.process(List.of(tempFile.toFile()));
        checker.destroy();

        double endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1_000_000.0; // ms

        Map<SeverityLevel, Integer> counts = counters.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getCount()));

        String output = counts.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));

        return List.of(
                TaskResults.CodeQualityResult.builder()
                        .criteria("java-checkstyle")
                        .score(1.0 /
                                (1 + counts.getOrDefault(SeverityLevel.ERROR, 0)
                                + 0.5 * counts.getOrDefault(SeverityLevel.WARNING, 0)
                                + 0.1 * counts.getOrDefault(SeverityLevel.INFO, 0)
                                + 0.00000000001 * counts.getOrDefault(SeverityLevel.IGNORE, 0))
                        )
                        .executorClass(CheckstyleEvaluator.class.getName())
                        .unit("1/errors")
                        .timeMillis(executionTime)
                        .output(output + "\n" + errorsTextStream.toString())
                        .build()
        );
    }
}
