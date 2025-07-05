package org.donutellko.modularbench.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
public class TaskSource {
    private String version;
    private List<TaskDefinition> tasks = new ArrayList<>();

    @Data
    public static class TaskDefinition {
        private String name;
        private String type;
        private TaskDifficulty difficulty;
        /* examples: math, arrays, spring framework */
        private String area;
        /* from which benchmark did we get this task */
        private String source;
        /* java, python, custom -- not every criteria is available for custom languages */
        private List<String> languages = new ArrayList<String>();
        /*
        * available_parameters: # mostly True/False
          - should-generate-tests # if the LLM should generate tests for the code
          - use-llm-judge         # if we want to check the results with an LLM-judge
          - all-tests-public      # if we want to give the LLM all tests as a reference
          - all-tests-hidden      # if we don't want to give any tests as a reference
          - should-use-libraries  # if we want the solution to use foreign dependencies
          - etc.
        * */
        private List<String> availableParameters = new ArrayList<String>();
        /*
        available_criteria:
          - ram_usage         # only for languages that we can run in a sandbox
          - cpu_usage         # only for languages that we can run in a sandbox
          - sonarqube         # for all languages
          - llm-judge-code-quality    # for all languages
          - llm-judge-comment-quality # for all languages
          - java-jacoco       # only for jvm languages
          - java-codestyle    # only for java
          - python-pyright    # only for python
          */
        private List<String> availableCriteria = new ArrayList<String>();
        private TaskDescription task;
        private GoldenSolution goldenSolution;
        /* Only if "use-llm-judge" is set to True */
        private String llmJudgePrompt;
    }

    static class GoldenSolution extends HashMap<String, String> {
    }

    @Data
    public static class TaskDescription {
        private String commonPrompt;
        private HashMap<String, LanguageSpecificTask> languagesSpecific;
    }

    @Data
    public static class LanguageSpecificTask {
        private String description;
        private List<TestDefinition> publicTests = new ArrayList<>();
        private List<TestDefinition> hiddenTests = new ArrayList<>();
    }

    @Data
    public static class TestDefinition {
        private String code;
    }
}
