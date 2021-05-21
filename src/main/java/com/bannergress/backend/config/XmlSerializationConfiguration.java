package com.bannergress.backend.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;

@Configuration
public class XmlSerializationConfiguration {
    @Bean
    public MappingJackson2XmlHttpMessageConverter mappingJackson2XmlHttpMessageConverter(Jackson2ObjectMapperBuilder builder) {
        XmlMapper mapper = builder.createXmlMapper(true).build();
        mapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        return new MappingJackson2XmlHttpMessageConverter(mapper);
    }
}
