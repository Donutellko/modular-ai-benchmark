package org.donutellko.modularbench;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskSource;

@Data
@AllArgsConstructor
public class TaskResult {
    private TaskSource taskSource;
    private TaskSource.TaskDefinition taskDefinition;
    private ExecutionConfig executionConfig;
    private String language;
    private String llmName;
    private String prompt;
    private String llmResponse;
}
