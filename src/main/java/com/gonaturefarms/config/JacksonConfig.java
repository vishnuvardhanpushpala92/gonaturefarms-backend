package com.gonaturefarms.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Explicitly configures Jackson 2 (classic {@code com.fasterxml.jackson.databind.ObjectMapper})
 * as the JSON engine Spring MVC actually uses for every REST request/response body, with a
 * global snake_case property naming strategy.
 * <p>
 * Why this is explicit rather than left to {@code spring.jackson.property-naming-strategy} in
 * application.properties: The entire frontend/backend contract in this app depends on every
 * controller consistently emitting/accepting snake_case JSON (to match the original database
 * column names the frontend already expects — see api/client.js on the frontend). This class
 * removes any JSON-capable converter Boot auto-configured and installs one backed by an explicit,
 * hand-built Jackson 2 ObjectMapper as the sole/primary JSON converter, so the naming strategy
 * is guaranteed regardless of Boot's internal auto-configuration.
 */
@Configuration
public class JacksonConfig implements WebMvcConfigurer {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Remove whichever JSON converter(s) Boot auto-configured (Jackson 2 and/or Jackson 3
        // based) so there's no ambiguity about which one Spring MVC picks for application/json.
        converters.removeIf(c -> c.getSupportedMediaTypes().contains(MediaType.APPLICATION_JSON));
        converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper()));
    }
}
