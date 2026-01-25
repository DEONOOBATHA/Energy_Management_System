package com.sscl.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * AI-Driven Customer Support using Google Gemini API
 */
@Service
@Slf4j
public class AIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${google.ai.api.key:}")
    private String apiKey;

    @Value("${google.ai.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent}")
    private String apiUrl;

    @Value("${chat.ai-enabled:true}")
    private boolean aiEnabled;

    public AIService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("🔑 AIService initialized:");
        log.info("  - API Key: {}", apiKey != null && !apiKey.isEmpty() ? "SET (length: " + apiKey.length() + ")" : "NOT SET");
        log.info("  - API URL: {}", apiUrl);
        log.info("  - AI Enabled: {}", aiEnabled);
    }

    /**
     * Generate AI response using Google Gemini API
     */
    public String generateResponse(String userMessage, String username) {
        log.info("🚀 generateResponse called - aiEnabled: {}, apiKey: {}", 
                 aiEnabled, (apiKey != null && !apiKey.isEmpty() ? "SET" : "NOT SET"));
        
        if (!aiEnabled) {
            log.warn("AI service is disabled");
            return getFallbackResponse();
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("❌ Google AI API key not configured - returning fallback");
            return getFallbackResponse();
        }

        try {
            String prompt = buildPrompt(userMessage, username);
            String requestBody = buildGeminiRequestBody(prompt);
            String fullUrl = apiUrl + "?key=" + apiKey;

            log.info("Sending request to Google Gemini API");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("❌ Google Gemini API error: {} - {}", response.getStatusCode(), response.getBody());
                return getFallbackResponse();
            }

            String responseBody = response.getBody();
            log.info("✅ Google Gemini API success");
            return extractGeminiResponse(responseBody);

        } catch (Exception e) {
            log.error("❌ Error calling Google Gemini API: {}", e.getMessage(), e);
            return getFallbackResponse();
        }
    }

    private String buildPrompt(String userMessage, String username) {
        return String.format(
            "You are a helpful customer support assistant for an energy management system. " +
            "Answer this question from %s in a clear, concise way (max 2-3 sentences): %s",
            username, userMessage
        );
    }

    private String buildGeminiRequestBody(String prompt) throws Exception {
        String json = String.format(
            "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}",
            prompt.replace("\"", "\\\"").replace("\n", "\\n")
        );
        return json;
    }

    private String extractGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            // Gemini response structure: candidates[0].content.parts[0].text
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                String text = candidates.get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
                
                if (!text.isEmpty()) {
                    log.info("Successfully extracted AI response: {} chars", text.length());
                    return text.trim();
                }
            }

            log.warn("Could not parse Gemini response");
            return getFallbackResponse();

        } catch (Exception e) {
            log.error("Error parsing Gemini response: {}", e.getMessage());
            return getFallbackResponse();
        }
    }

    private String getFallbackResponse() {
        return "I apologize, but I'm having trouble processing your request at the moment. " +
               "An administrator has been notified and will respond to your inquiry shortly. " +
               "Thank you for your patience!";
    }

    public boolean isAiEnabled() {
        return aiEnabled;
    }
}
