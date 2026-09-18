package com.karthik.incidentmanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    // Set APP_SERVER_URL in Railway's env vars to the production URL.
    // Leave it unset locally so Springdoc auto-detects the current host (e.g. localhost:8080).
    @Value("${app.server-url:}")
    private String serverUrl;

    @Bean
    public OpenAPI customOpenAPI() {

        final String securitySchemeName = "bearerAuth";

        OpenAPI openAPI = new OpenAPI();

        if (serverUrl != null && !serverUrl.isBlank()) {
            openAPI.servers(List.of(
                    new Server()
                            .url(serverUrl)
                            .description("Production Server")
            ));
        }

        return openAPI
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(securitySchemeName)
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        securitySchemeName,
                                        new SecurityScheme()
                                                .name("Authorization")
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }
}
