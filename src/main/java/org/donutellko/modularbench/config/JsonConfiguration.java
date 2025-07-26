package org.donutellko.modularbench.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JsonConfiguration {

    @Bean
    @Primary
    public ObjectMapper jsonMapper() {
        return new ObjectMapper();
    }
}
