package org.donutellko.modularbench.controller;

import org.apache.commons.io.FileUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:5173")
public class FileController {
    private static final Map<String, String> DIRECTORIES = Map.of(
        "exec_configs", "exec_configs",
        "task_sources", "task_sources",
        "bench_status", "bench_status",
        "bench_results", "bench_results"
    );

    @PostConstruct
    public void init() {
        DIRECTORIES.values().forEach(dir -> {
            File directory = new File(dir);
            if (!directory.exists() && !directory.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + dir);
            }
        });
    }

    @GetMapping(value = "/{directory}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> listFiles(@PathVariable String directory) {
        if (!DIRECTORIES.containsKey(directory)) {
            return ResponseEntity.badRequest().build();
        }

        File dir = new File(DIRECTORIES.get(directory));
        if (!dir.exists() && !dir.mkdirs()) {
            return ResponseEntity.internalServerError().build();
        }

        List<String> files = FileUtils.listFiles(dir, new String[]{"yaml", "yml"}, false)
            .stream()
            .map(File::getName)
            .toList();

        return ResponseEntity.ok(files);
    }

    @GetMapping(value = "/{directory}/{filename}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getFile(
            @PathVariable String directory,
            @PathVariable String filename) throws IOException {
        if (!DIRECTORIES.containsKey(directory)) {
            return ResponseEntity.badRequest().build();
        }

        File file = new File(DIRECTORIES.get(directory), filename);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Files.readString(file.toPath()));
    }

    @PutMapping(value = "/{directory}/{filename}",
                consumes = MediaType.TEXT_PLAIN_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateFile(
            @PathVariable String directory,
            @PathVariable String filename,
            @RequestBody String content) throws IOException {
        if (!DIRECTORIES.containsKey(directory)) {
            return ResponseEntity.badRequest().build();
        }

        File dir = new File(DIRECTORIES.get(directory));
        if (!dir.exists() && !dir.mkdirs()) {
            return ResponseEntity.internalServerError().build();
        }

        // Backup first
        File file = new File(dir, filename);
        if (file.exists()) {
            File backupDir = new File(directory + "_backups/" + filename.replaceFirst("[.][^.]+$", ""));
            if (!backupDir.exists() && !backupDir.mkdirs()) {
                return ResponseEntity.internalServerError().build();
            }

            Files.copy(file.toPath(),
                Path.of(backupDir.getPath(), Instant.now().getEpochSecond() + "_" + filename));
        }

        Files.writeString(file.toPath(), content);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{directory}/{filename}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable String directory,
            @PathVariable String filename) throws IOException {
        if (!DIRECTORIES.containsKey(directory)) {
            return ResponseEntity.badRequest().build();
        }

        File file = new File(DIRECTORIES.get(directory), filename);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        if (!file.delete()) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok().build();
    }
}
