package com.karthik.incidentmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    /**
     * Without explicit timeouts, RestTemplate will wait indefinitely on a
     * slow or hung Groq API call, which would eventually exhaust the async
     * triage thread pool and quietly stall AI triage for every incident.
     * <p>
     * Built directly with SimpleClientHttpRequestFactory instead of
     * RestTemplateBuilder so this bean has no dependency on Spring Boot's
     * web-client autoconfiguration being present/resolved.
     */
    @Bean
    public RestTemplate restTemplate() {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);  // ms
        factory.setReadTimeout(15_000);    // ms

        return new RestTemplate(factory);
    }

    // No custom ObjectMapper bean here on purpose: Spring Boot's
    // autoconfigured ObjectMapper already has JavaTimeModule registered
    // (needed for the LocalDateTime fields on IncidentResponseDto,
    // CommentResponseDto, etc.) and is injected into AiProviderService
    // automatically. Defining `new ObjectMapper()` here would replace that
    // bean app-wide - including the one Spring MVC uses to serialize every
    // JSON response - and a plain ObjectMapper has no JavaTimeModule, so it
    // would break serialization of any DTO with a LocalDateTime field.
}
