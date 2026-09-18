package com.karthik.incidentmanagement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karthik.incidentmanagement.dto.AiResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Talks to the Groq (OpenAI-compatible) chat completions API to triage an
 * incident: category, severity, and a short recommendation.
 * <p>
 * The model is asked to reply in strict JSON so parsing is a straightforward
 * Jackson read rather than scraping "Label: value" lines out of free text.
 * If the model still returns something malformed, or the call fails or times
 * out, we fall back to a safe default so a flaky third-party AI call can
 * never break incident creation.
 */
@Service
@RequiredArgsConstructor
public class AiProviderService {

    private static final Logger log = LoggerFactory.getLogger(AiProviderService.class);

    private static final AiResponseDto FALLBACK = new AiResponseDto(
            "Unknown",
            "MEDIUM",
            "AI triage is temporarily unavailable. Please review this incident manually."
    );

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    @Value("${groq.model:llama-3.1-8b-instant}")
    private String model;

    public AiResponseDto analyzeIncident(String description) {

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GROQ_API_KEY is not configured — skipping AI triage and returning fallback result");
            return FALLBACK;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user", "content", "Incident description:\n" + description)
                    ),
                    "temperature", 0.2,
                    "response_format", Map.of("type", "json_object")
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            String content = objectMapper.readTree(response.getBody())
                    .path("choices").get(0)
                    .path("message").path("content")
                    .asText();

            return parseModelOutput(content);

        } catch (ResourceAccessException e) {
            log.error("Groq API call timed out or was unreachable", e);
            return FALLBACK;
        } catch (Exception e) {
            log.error("AI triage failed unexpectedly", e);
            return FALLBACK;
        }
    }

    /** Parses the model's JSON reply; falls back to a lenient line scan if the model didn't comply. */
    private AiResponseDto parseModelOutput(String content) {
        try {
            JsonNode json = objectMapper.readTree(content);
            return new AiResponseDto(
                    json.path("category").asText("Unknown"),
                    json.path("severity").asText("MEDIUM"),
                    json.path("recommendation").asText("No recommendation returned.")
            );
        } catch (Exception jsonParseFailure) {
            log.warn("Model did not return valid JSON, falling back to line-based parsing. Raw content: {}", content);
            return parseLegacyFormat(content);
        }
    }

    private AiResponseDto parseLegacyFormat(String response) {
        String category = "Unknown";
        String severity = "MEDIUM";
        String recommendation = "No recommendation returned.";

        for (String line : response.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("Category:")) {
                category = trimmed.substring("Category:".length()).trim();
            } else if (trimmed.startsWith("Severity:")) {
                severity = trimmed.substring("Severity:".length()).trim();
            } else if (trimmed.startsWith("Recommendation:")) {
                recommendation = trimmed.substring("Recommendation:".length()).trim();
            }
        }
        return new AiResponseDto(category, severity, recommendation);
    }

    private static final String SYSTEM_PROMPT = """
            You are a Senior Site Reliability Engineer triaging a production incident.
            Reply with ONLY a JSON object, no prose, no markdown fences, matching exactly:
            {
              "category": "Database|Network|Application|Security|Infrastructure",
              "severity": "LOW|MEDIUM|HIGH|CRITICAL",
              "recommendation": "one short, actionable sentence"
            }
            """;
}
