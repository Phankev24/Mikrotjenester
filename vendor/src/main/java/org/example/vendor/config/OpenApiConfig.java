package org.example.vendor.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfigurasjon for Swagger uten autentiseringskrav eller Authorize-knapp,
 * slik at du kan teste endepunktene sømløst uten frontend.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApi() {
        return new OpenAPI();
    }
}