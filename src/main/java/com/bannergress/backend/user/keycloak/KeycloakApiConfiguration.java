package com.bannergress.backend.user.keycloak;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
class KeycloakApiConfiguration {
    @Bean
    KeycloakApi keycloakApi(RestClient.Builder builder, OAuth2AuthorizedClientManager authorizedClientManager,
                            @Value("${keycloak.auth-server-url}") String serverUrl,
                            @Value("${keycloak.realm}") String realm) {
        OAuth2ClientHttpRequestInterceptor requestInterceptor = new OAuth2ClientHttpRequestInterceptor(
            authorizedClientManager);
        requestInterceptor.setClientRegistrationIdResolver(request -> "keycloak");
        String baseUrl = String.format("%s/admin/realms/%s", serverUrl, realm);
        RestClientAdapter exchangeAdapter = RestClientAdapter
            .create(builder.requestInterceptor(requestInterceptor).baseUrl(baseUrl).build());
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(exchangeAdapter).build();
        return factory.createClient(KeycloakApi.class);
    }
}
