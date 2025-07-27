package org.donutellko.modularbench.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskSource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class BenchmarkProgressTracker {
    private final ConcurrentHashMap<String, BenchmarkStatus> activeRuns = new ConcurrentHashMap<>();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @Data
    public static class BenchmarkStatus {
        private final String execConfigFile;
        private final Map<String, TaskSourceStatus> taskSourceStatuses;
        private final String resultFilename;

        public TaskSourceStatus getTaskSourceStatus(TaskSource taskSource) {
            return taskSourceStatuses.computeIfAbsent(taskSource.getName(),
                name -> new TaskSourceStatus((int) taskSource.getTasks().stream().count()));
        }
    }

    @Data
    public static class TaskSourceStatus {
        private final int total;
        private int completed;
        private int filteredOut;
        private int error;

        public synchronized void incrementFilteredOut() {
            filteredOut++;
            saveIfNeeded();
        }

        public synchronized void incrementCompleted() {
            completed++;
            saveIfNeeded();
        }

        public synchronized void incrementError() {
            error++;
            saveIfNeeded();
        }

        private void saveIfNeeded() {
            // Status is managed by the parent BenchmarkStatus
        }
    }

    @SneakyThrows
    public BenchmarkStatus createStatus(ExecutionConfig execConfigFile, Iterable<TaskSource> taskSourceList, String resultFilename) {

        Map<String, BenchmarkProgressTracker.TaskSourceStatus> taskSourceStatuses = new HashMap<>();
        for (TaskSource taskSource : taskSourceList) {
            int totalTasks = (int) taskSource.getTasks().stream().count();
            taskSourceStatuses.put(taskSource.getName(), new BenchmarkProgressTracker.TaskSourceStatus(totalTasks));
        }

        BenchmarkStatus status = new BenchmarkStatus(execConfigFile.getFilepath(), taskSourceStatuses, resultFilename);
        activeRuns.put(resultFilename, status);
        saveStatus(status);
        return status;
    }

    public BenchmarkStatus getStatus(String statusFile) {
        return activeRuns.get(statusFile);
    }

    public void completeRun(String statusFile) {
        BenchmarkStatus status = activeRuns.remove(statusFile);
        if (status != null) {
            try {
                saveStatus(status);
            } catch (IOException e) {
                // Log error but don't throw as this is cleanup code
                e.printStackTrace();
            }
        }
    }

    private void saveStatus(BenchmarkStatus status) throws IOException {
        Path statusPath = Paths.get("bench_status", status.getResultFilename());
        Files.createDirectories(statusPath.getParent());
        String yamlContent = yamlMapper.writeValueAsString(status);
        Files.writeString(statusPath, yamlContent);
    }
}
