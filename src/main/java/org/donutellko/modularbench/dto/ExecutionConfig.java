package org.donutellko.modularbench.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExecutionConfig {
    private Set<TaskDifficulty> difficulties;
    private Set<String> areas;
    private List<String> languages = new ArrayList<String>();
    private List<String> parameters = new ArrayList<String>();
    private List<String> criteria = new ArrayList<String>();
}
