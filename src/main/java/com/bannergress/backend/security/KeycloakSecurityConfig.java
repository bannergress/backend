package com.bannergress.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.CacheControlConfig;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

/** Keycloak security configuration. */
@Configuration
@EnableMethodSecurity(jsr250Enabled = true, prePostEnabled = true)
@Profile("!dev")
public class KeycloakSecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http //
            .csrf(CsrfConfigurer::disable) //
            .headers(this::customizeHeaders)
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())) //
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) //
            .build();
    }

    private HeadersConfigurer<HttpSecurity> customizeHeaders(HeadersConfigurer<HttpSecurity> customizer) {
        return customizer //
            .cacheControl(CacheControlConfig::disable)
            .addHeaderWriter(new StaticHeadersWriter(HttpHeaders.CACHE_CONTROL, "must-revalidate"));
    }
}
