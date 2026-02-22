package com.bannergress.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.CacheControlConfig;
import org.springframework.security.oauth2.server.resource.authentication.ExpressionJwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

import static org.springframework.security.config.Customizer.withDefaults;

/** Keycloak security configuration. */
@Configuration
@EnableMethodSecurity(jsr250Enabled = true, prePostEnabled = true)
class KeycloakSecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) {
        return http //
            .csrf(CsrfConfigurer::disable) //
            .headers(this::customizeHeaders) //
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults())) //
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) //
            .build();
    }

    private HeadersConfigurer<HttpSecurity> customizeHeaders(HeadersConfigurer<HttpSecurity> customizer) {
        return customizer //
            .cacheControl(CacheControlConfig::disable)
            .addHeaderWriter(new StaticHeadersWriter(HttpHeaders.CACHE_CONTROL, "must-revalidate"));
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        var converter = new ExpressionJwtGrantedAuthoritiesConverter(
            new SpelExpressionParser().parseRaw("[realm_access][roles]"));
        converter.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
        return jwtConverter;
    }
}
