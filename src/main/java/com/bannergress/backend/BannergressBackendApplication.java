package com.bannergress.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Main application.
 */
@SpringBootApplication
public class BannergressBackendApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplicationBuilder(BannergressBackendApplication.class).build();
        application.run(args);
    }
}
