package org.donutellko.modularbench;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.donutellko.modularbench.dto.TaskSource;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParseYamlTest {

    @Test
    public void testParse() {
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        ObjectMapper mapper = new ObjectMapper(yamlFactory);
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
//        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        try (InputStream inputStream = ParseYamlTest.class.getClassLoader().getResourceAsStream("config-example-1.yaml")) {
            assertNotNull(inputStream);
            if (inputStream == null) {
                System.err.println("YAML file not found!");
                return;
            }
            TaskSource config = mapper.readValue(inputStream, TaskSource.class);
            System.out.println(config);
            assertNotNull(config);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}