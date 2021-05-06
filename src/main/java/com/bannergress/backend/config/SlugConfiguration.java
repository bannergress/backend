package com.bannergress.backend.config;

import com.bannergress.backend.utils.SlugGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for slugs.
 */
@Configuration
public class SlugConfiguration {
    @Bean
    public SlugGenerator slugGenerator() {
        return new SlugGenerator(2);
    }
}
