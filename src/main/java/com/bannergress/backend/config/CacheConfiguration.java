package com.bannergress.backend.config;

import okhttp3.Cache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * Configuration for caches.
 */
@Configuration
public class CacheConfiguration {
    @Bean
    public Cache pictureCache(@Value("${picture.cache.directory:caches/pictures/}") final String cacheDirectory,
                              @Value("${picture.cache.size:1000000000}") final long cacheSize) {
        return new Cache(new File(cacheDirectory), cacheSize);
    }
}
