package com.bannergress.backend.restrictedarea.overpass;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
class OverpassClientConfiguration {
    private static final String USER_AGENT = "Bannergress";

    @Bean
    OverpassApi overpassApi(RestClient.Builder builder) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(builder.defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT).build()))
            .build();
        return factory.createClient(OverpassApi.class);
    }
}
