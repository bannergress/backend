package com.bannergress.backend.banner.timezone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
class TimezoneDbApiConfiguration {
    @Bean
    TimezoneDbApi timezoneDbApi(RestClient.Builder builder,
                                @Value("${timezonedb.base-url:https://api.timezonedb.com}") String baseUrl) {
        RestClientAdapter restClientAdapter = RestClientAdapter.create(builder.baseUrl(baseUrl).build());
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(restClientAdapter).build();
        return factory.createClient(TimezoneDbApi.class);
    }
}
