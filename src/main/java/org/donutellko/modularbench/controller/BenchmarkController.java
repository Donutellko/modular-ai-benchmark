package org.donutellko.modularbench.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/benchmark")
@CrossOrigin(origins = "http://localhost:5173")
public class BenchmarkController {
    private final Map<String, RunStatus> activeRuns = new ConcurrentHashMap<>();

    record RunRequest(String config, List<String> tasks, String resultFile) {}
    record RunResponse(String id) {}
    record RunStatus(String status, Map<String, TaskProgress> progress) {}
    record TaskProgress(int total, int completed, int filtered, int error, int inProgress) {}

    @PostMapping("/run")
    public ResponseEntity<RunResponse> startRun(@RequestBody RunRequest request) throws IOException {
        String id = request.resultFile();

        // Create status file
        File statusFile = new File("bench_status", id);
        Files.writeString(statusFile.toPath(), """
            status: running
            config: %s
            tasks: %s
            startTime: %d
            """.formatted(request.config, request.tasks, Instant.now().getEpochSecond()));

        // Initialize progress tracking
        Map<String, TaskProgress> progress = new ConcurrentHashMap<>();
        for (String task : request.tasks) {
            progress.put(task, new TaskProgress(100, 0, 0, 0, 0));
        }
        activeRuns.put(id, new RunStatus("running", progress));

        // TODO: Start actual benchmark execution
        // For now, just simulate progress updates
        new Thread(() -> simulateProgress(id)).start();

        return ResponseEntity.ok(new RunResponse(id));
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<RunStatus> getStatus(@PathVariable String id) {
        RunStatus status = activeRuns.get(id);
        return status != null ? ResponseEntity.ok(status) : ResponseEntity.notFound().build();
    }

    // Temporary simulation
    private void simulateProgress(String id) {
        RunStatus status = activeRuns.get(id);
        if (status == null) return;

        try {
            for (String task : status.progress.keySet()) {
                for (int i = 0; i < 100; i += 10) {
                    Thread.sleep(1000);
                    status.progress.put(task, new TaskProgress(100, i, 10, 0, 10));
                }
            }
            activeRuns.put(id, new RunStatus("completed", status.progress));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
