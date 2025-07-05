package org.donutellko.modularbench.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.donutellko.modularbench.dto.BenchResults;
import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskSource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class FileService {

    private final ObjectMapper mapper;

    public ExecutionConfig readExecConfig(String filePath) {
        return readYaml(filePath, ExecutionConfig.class);
    }

    public TaskSource readTaskSource(String filePath) {
        TaskSource taskSource = readYaml(filePath, TaskSource.class);
        taskSource.setPath(filePath);
        if (taskSource.getName() == null || taskSource.getName().isEmpty()) {
            taskSource.setName(Paths.get(filePath).getFileName().toString());
        }
        return taskSource;
    }

    public void writeBenchResults(String filePath, BenchResults benchResults) {
        writeYaml(filePath, benchResults);
    }

    @SneakyThrows
    public <T> T readYaml(String filePath, Class<T> clazz) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            return readYaml(inputStream, clazz);
        }
    }

    @SneakyThrows
    public <T> T readYaml(InputStream inputStream, Class<T> clazz) {
        T obj = mapper.readValue(inputStream, clazz);
        return obj;
    }

    @SneakyThrows
    public <T> void writeYaml(String filePath, T obj) {
        mapper.writeValue(Files.newOutputStream(Paths.get(filePath)), obj);
    }
}
