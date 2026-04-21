package com.example.products.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_JWT_SCHEME = "bearer-jwt";

    @Bean
    public OpenAPI productManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Manager API")
                        .version("0.0.1")
                        .description("REST API for managing products"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_JWT_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "Paste the access token from POST /auth/register or POST /auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_JWT_SCHEME));
    }
}
