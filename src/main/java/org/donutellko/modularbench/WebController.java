package org.donutellko.modularbench;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WebController {
    private final BenchmarkService benchmarkService;
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ObjectMapper jsonMapper = new ObjectMapper();

    @PostMapping("/benchmark/start")
    public ResponseEntity<?> startBenchmark(@RequestBody StartBenchmarkRequest request) {
        try {
            String statusFile = benchmarkService.startBenchmark(
                request.execConfig(),
                request.taskSources(),
                request.resultFilename()
            );
            return ResponseEntity.ok(Map.of("statusFile", statusFile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/benchmark/status/{statusFile}")
    public ResponseEntity<?> getBenchmarkStatus(@PathVariable String statusFile) {
        try {
            String yamlContent = Files.readString(Paths.get("bench_status", statusFile));
            // Convert YAML to Object and then to JSON
            Object status = yamlMapper.readValue(yamlContent, Object.class);
            return ResponseEntity.ok(status);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
