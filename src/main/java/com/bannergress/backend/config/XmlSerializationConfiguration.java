package com.bannergress.backend.config;

import org.springframework.boot.jackson.autoconfigure.XmlMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.dataformat.xml.XmlWriteFeature;

@Configuration
class XmlSerializationConfiguration {
    @Bean
    XmlMapperBuilderCustomizer xmlMapperBuilderCustomizer() {
        return builder -> builder.enable(XmlWriteFeature.WRITE_XML_DECLARATION);
    }
}
