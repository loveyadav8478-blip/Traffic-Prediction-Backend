package com.loveyadav.traffic_pred.service;

import com.loveyadav.traffic_pred.exception.AIServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class MistralService {

    private static final Logger log = LoggerFactory.getLogger(MistralService.class);

    private static final String MISTRAL_URL =
            "https://api.mistral.ai/v1/chat/completions";

    @Value("${mistral.api.key}")
    private String apiKey;

    @Value("${mistral.model:mistral-small-latest}")
    private String model;

    @Value("${mistral.max-tokens:512}")
    private int maxTokens;

    @Value("${mistral.temperature:0.3}")
    private double temperature;

    private final RestTemplate restTemplate;

    public MistralService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generate(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // Mistral uses OpenAI-style messages format
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", List.of(message));
        body.put("max_tokens", maxTokens);
        body.put("temperature", temperature);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    MISTRAL_URL, HttpMethod.POST, entity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new AIServiceException("Mistral returned: " + response.getStatusCode());
            }

            return extractText(response.getBody());

        } catch (AIServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Mistral API call failed: {}", e.getMessage(), e);
            throw new AIServiceException("Mistral API unavailable: " + e.getMessage());
        }
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private String extractText(Map<?, ?> body) {
        try {
            List choices = (List) body.get("choices");
            if (choices == null || choices.isEmpty())
                throw new AIServiceException("Mistral returned no choices");

            Map choice  = (Map) choices.get(0);
            Map message = (Map) choice.get("message");
            String text = (String) message.get("content");

            if (text == null || text.isBlank())
                throw new AIServiceException("Mistral returned empty text");

            return text.trim();
        } catch (AIServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse Mistral response: {}", body, e);
            throw new AIServiceException("Could not parse Mistral response");
        }
    }
}