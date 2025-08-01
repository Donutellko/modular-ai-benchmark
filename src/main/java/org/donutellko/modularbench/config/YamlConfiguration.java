package org.donutellko.modularbench.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class YamlConfiguration {

    @Bean
    public YAMLFactory yamlFactory() {
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        yamlFactory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
        yamlFactory.enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE);
        yamlFactory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        yamlFactory.enable(YAMLGenerator.Feature.SPLIT_LINES);
        yamlFactory.enable(YAMLGenerator.Feature.USE_PLATFORM_LINE_BREAKS);
        return yamlFactory;
    }

    @Bean("yamlMapper")
    public YAMLMapper yamlMapper(YAMLFactory yamlFactory) {
        YAMLMapper mapper = new YAMLMapper(yamlFactory);
        mapper.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
        mapper.enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE);
        mapper.enable(YAMLGenerator.Feature.SPLIT_LINES);
        mapper.enable(YAMLGenerator.Feature.USE_PLATFORM_LINE_BREAKS);
        mapper.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }
}
