package org.donutellko.modularbench;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.donutellko.modularbench.dto.TaskDifficulty;
import org.donutellko.modularbench.dto.TaskSource;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ParseTaskSourceTest {

    @Test
    public void testParse() throws Exception {
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        ObjectMapper mapper = new ObjectMapper(yamlFactory);
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
//        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        InputStream inputStream = ParseTaskSourceTest.class.getClassLoader().getResourceAsStream("task-source-example-1.yaml");
        assertNotNull(inputStream);
        TaskSource config = mapper.readValue(inputStream, TaskSource.class);
        System.out.println(config);
        assertThat(config.getVersion()).isEqualTo("1.0");
        assertThat(config.getTasks()).hasSize(1);
        TaskSource.TaskDefinition taskDefinition = config.getTasks().get(0);
        assertThat(taskDefinition.getName()).isEqualTo("highest common factor, implementation from zero");
        assertThat(taskDefinition.getType()).isEqualTo("implementation from zero");
        assertThat(taskDefinition.getDifficulty()).isEqualTo(TaskDifficulty.EASY);
        assertThat(taskDefinition.getArea()).isEqualTo("math");
        assertThat(taskDefinition.getSource()).isEqualTo("MBPP");
        assertThat(taskDefinition.getLanguages()).containsExactlyInAnyOrder("java", "python", "custom");
        assertThat(taskDefinition.getAvailableParameters()).containsExactlyInAnyOrder(
                "should-generate-tests",
                "use-llm-judge",
                "all-tests-public",
                "all-tests-hidden",
                "should-use-libraries"
        );
        assertThat(taskDefinition.getAvailableCriteria()).containsExactlyInAnyOrder(
                "ram_usage",
                "cpu_usage",
                "sonarqube",
                "llm-judge-code-quality",
                "llm-judge-comment-quality",
                "java-jacoco",
                "java-codestyle",
                "python-pyright"
        );
        TaskSource.TaskDescription taskDescription = taskDefinition.getTask();
        assertThat(taskDescription.getCommonPrompt()).startsWith("Write a function in ${language} to calculate the \n" +
                "highest common factor of two numbers.");
        assertThat(taskDescription.getLanguagesSpecific()).hasSize(2);
        TaskSource.LanguageSpecificTask pythonTask = taskDescription.getLanguagesSpecific().get("python");
        assertThat(pythonTask.getDescription()).isEqualTo("${common_prompt}\n");
        assertThat(pythonTask.getPublicTests()).hasSize(1);
        assertThat(pythonTask.getPublicTests().get(0).getCode())
                .isEqualTo("""
                        result = ${solution.function_name}(10, 15)
                        assert result == 5
                        """);
        assertThat(pythonTask.getHiddenTests()).hasSize(2);
    }
}