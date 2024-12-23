package com.bannergress.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

/**
 * Configuration for ETag generation.
 */
@Configuration
public class EtagConfiguration {
    @Bean
    ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
        ShallowEtagHeaderFilter result = new ShallowEtagHeaderFilter();
        result.setWriteWeakETag(true);
        return result;
    }
}
