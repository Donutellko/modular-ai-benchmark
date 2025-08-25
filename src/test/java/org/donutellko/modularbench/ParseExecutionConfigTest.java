package org.donutellko.modularbench;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.ExecutionConfig.ExecutionParameter;
import org.donutellko.modularbench.dto.TaskDifficulty;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ParseExecutionConfigTest {

    @Test
    public void testParse() throws Exception {
        ExecutionConfig expected = ExecutionConfig.builder()
                .version("1.0")
                .difficulties(Set.of(TaskDifficulty.EASY, TaskDifficulty.HARD, TaskDifficulty.MEDIUM))
                .languages(Set.of("java"))
                .parameters(Set.of(
                        new ExecutionParameter("should-generate-tests", false),
                        new ExecutionParameter("use-llm-judge", true),
                        new ExecutionParameter("all-tests-public", false),
                        new ExecutionParameter("should-use-libraries", false),
                        new ExecutionParameter("all-tests-hidden", true)
                ))
                .criteria(Set.of(
                        new ExecutionParameter("unit-test", true),
                        new ExecutionParameter("ram-usage", true),
                        new ExecutionParameter("cpu-usage", true),
                        new ExecutionParameter("sonarqube", true),
                        new ExecutionParameter("llm-judge-code-quality", true),
                        new ExecutionParameter("llm-judge-comment-quality", true),
                        new ExecutionParameter("java-jacoco", true),
                        new ExecutionParameter("java-checkstyle", true),
                        new ExecutionParameter("java-pmd", true),
                        new ExecutionParameter("python-pyright", false)
                ))
                .llmJudge("mistralai/devstral-small-2505:free")
                .llms(Set.of("stub-llm", "mistralai/devstral-small-2505:free"))
                .build();
        /*

        ExecutionConfig(difficulties=[EASY, HARD, MEDIUM], areas=null, languages=[java], parameters=[use-llm-judge], criteria=[llm-judge-code-quality, java-checkstyle, ram_usage, sonarqube, java-jacoco, llm-judge-comment-quality, cpu_usage])

         */


        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        ObjectMapper mapper = new ObjectMapper(yamlFactory);
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
//        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        InputStream inputStream = ParseExecutionConfigTest.class.getClassLoader().getResourceAsStream("exec-config-example-1.yaml");

        assertNotNull(inputStream);
        ExecutionConfig actual = mapper.readValue(inputStream, ExecutionConfig.class);
        System.out.println(actual);
        assertNotNull(actual);
        assertEquals(expected, actual);

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expected);
    }
}