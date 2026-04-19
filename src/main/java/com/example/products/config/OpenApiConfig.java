package com.example.products.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Manager API")
                        .version("0.0.1")
                        .description("REST API for managing products"));
    }
}
