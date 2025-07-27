package org.donutellko.modularbench;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class BenchmarkService {
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ConcurrentHashMap<String, BenchmarkStatus> activeRuns = new ConcurrentHashMap<>();

    public String startBenchmark(String execConfigFile, List<String> taskSourceFiles, String resultFilename) throws IOException {
        // Read the execution config
        Path execConfigPath = Paths.get("exec_configs", execConfigFile);
        if (!Files.exists(execConfigPath)) {
            throw new IllegalArgumentException("Execution config file not found: " + execConfigFile);
        }

        // Create status file
        Path statusFilePath = Paths.get("bench_status", resultFilename);
        Files.createDirectories(statusFilePath.getParent());

        // Initialize status for each task source
        Map<String, TaskSourceStatus> taskSourceStatuses = new HashMap<>();
        for (String taskSource : taskSourceFiles) {
            Path taskSourcePath = Paths.get("task_sources", taskSource);
            if (!Files.exists(taskSourcePath)) {
                throw new IllegalArgumentException("Task source file not found: " + taskSource);
            }

            // Count total tasks in the source
            Map<String, Object> taskSourceYaml = yamlMapper.readValue(Files.readString(taskSourcePath), Map.class);
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) taskSourceYaml.get("tasks");
            int totalTasks = tasks != null ? tasks.size() : 0;

            taskSourceStatuses.put(taskSource, new TaskSourceStatus(totalTasks));
        }

        // Create initial status
        BenchmarkStatus status = new BenchmarkStatus(
            execConfigFile,
            taskSourceFiles,
            resultFilename,
            taskSourceStatuses
        );

        // Save initial status
        saveStatus(status, statusFilePath);

        // Start benchmark execution asynchronously
        runBenchmarkAsync(status, execConfigPath, statusFilePath);

        return resultFilename;
    }

    @Async
    protected void runBenchmarkAsync(BenchmarkStatus status, Path execConfigPath, Path statusFilePath) {
        try {
            // Add to active runs
            activeRuns.put(status.resultFilename(), status);

            for (String taskSource : status.taskSourceFiles()) {
                TaskSourceStatus sourceStatus = status.taskSourceStatuses().get(taskSource);
                sourceStatus.inProgress(sourceStatus.total());
                saveStatus(status, statusFilePath);

                // TODO: Execute actual benchmark logic here using your existing benchmark execution code
                // This is just a simulation for now
                Thread.sleep(2000);

                sourceStatus.completed(sourceStatus.total());
                sourceStatus.inProgress(0);
                saveStatus(status, statusFilePath);
            }

            // Create final results file
            Path resultsPath = Paths.get("bench_results", status.resultFilename());
            Files.createDirectories(resultsPath.getParent());
            Files.writeString(resultsPath, "TODO: Add actual benchmark results here");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            activeRuns.remove(status.resultFilename());
        }
    }

    private void saveStatus(BenchmarkStatus status, Path statusFilePath) throws IOException {
        String yamlContent = yamlMapper.writeValueAsString(status);
        Files.writeString(statusFilePath, yamlContent);
    }

    record BenchmarkStatus(
        String execConfigFile,
        List<String> taskSourceFiles,
        String resultFilename,
        Map<String, TaskSourceStatus> taskSourceStatuses
    ) {}

    record TaskSourceStatus(
        int total,
        int completed,
        int inProgress,
        int filteredOut,
        int error
    ) {
        TaskSourceStatus(int total) {
            this(total, 0, 0, 0, 0);
        }

        TaskSourceStatus completed(int count) {
            return new TaskSourceStatus(total, count, inProgress, filteredOut, error);
        }

        TaskSourceStatus inProgress(int count) {
            return new TaskSourceStatus(total, completed, count, filteredOut, error);
        }

        TaskSourceStatus filteredOut(int count) {
            return new TaskSourceStatus(total, completed, inProgress, count, error);
        }

        TaskSourceStatus error(int count) {
            return new TaskSourceStatus(total, completed, inProgress, filteredOut, count);
        }
    }
}
