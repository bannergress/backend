package com.bannergress.backend.user.keycloak;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.support.OAuth2RestClientHttpServiceGroupConfigurer;

@Configuration
class KeycloakApiConfiguration {
    @Bean
    OAuth2RestClientHttpServiceGroupConfigurer oauth2Configurer(OAuth2AuthorizedClientManager authorizedClientManager) {
        return OAuth2RestClientHttpServiceGroupConfigurer.from(authorizedClientManager);
    }
}
