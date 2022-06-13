package com.bannergress.backend.config;

import com.bannergress.backend.utils.LanguageRangeResolver;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {
    @Bean
    public ParameterCustomizer languageRangeCustomizer() {
        return new LanguageRangeResolver();
    }
}
