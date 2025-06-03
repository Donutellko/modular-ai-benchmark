package org.donutellko.modularbench.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionConfig {
    private Set<TaskDifficulty> difficulties;
    private Set<String> areas;
    private Set<String> languages;
    private Set<ExecutionParameter> parameters;
    private Set<ExecutionParameter> criteria;
    private Set<String> llms;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionParameter {
        private String name;
        private Boolean enabled;
    }
}
