package org.donutellko.modularbench;

import org.donutellko.modularbench.dto.TaskSource;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class ParseYamlTest {

    @Test
    public void testParse() {
        LoaderOptions loadingConfig = new LoaderOptions();
        loadingConfig.setEnumCaseSensitive(false);
        loadingConfig.setAllowDuplicateKeys(false);

        Yaml yaml = new Yaml(new Constructor(TaskSource.class, loadingConfig));
        try (InputStream inputStream = ParseYamlTest.class.getClassLoader().getResourceAsStream("config-example-1.yaml")) {
            assertNotNull(inputStream);
            if (inputStream == null) {
                System.err.println("YAML file not found!");
                return;
            }
            TaskSource config = yaml.load(inputStream);
            System.out.println(config);
            assertNotNull(config);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}