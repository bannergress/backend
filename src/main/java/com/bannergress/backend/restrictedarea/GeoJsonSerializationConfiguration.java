package com.bannergress.backend.restrictedarea;

import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeoJsonSerializationConfiguration {
    @Bean
    JtsModule jtsModule() {
        return new JtsModule();
    }
}
