package com.anwar.aicodereview.service;

import com.anwar.aicodereview.exception.AiAnalysisException;
import com.anwar.aicodereview.exception.AiProviderConfigurationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RestTemplate restTemplate;

    @Value("${ai.gemini.api-key:}")
    private String apiKey;

    @Value("${ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String geminiBaseUrl;

    @Value("${ai.gemini.model:gemini-3.6-flash}")
    private String geminiModel;

    @Value("${ai.gemini.endpoint:}")
    private String geminiEndpoint;

    public AiAnalysisService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String analyzeCode(String code) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiProviderConfigurationException("Google API Key is missing. Please configure GOOGLE_API_KEY.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String promptText = "You are a senior software engineer. Analyze the following code:\n"
                + "- Identify bugs\n"
                + "- Suggest improvements\n"
                + "- Suggest optimized code version\n"
                + "- Indicate potential security issues\n\n"
                + "Code:\n" + code;

        Map<String, Object> body = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> contentPart = new HashMap<>();
        List<Map<String, String>> parts = new ArrayList<>();
        Map<String, String> textPart = new HashMap<>();
        textPart.put("text", promptText);
        parts.add(textPart);
        contentPart.put("parts", parts);
        contents.add(contentPart);
        body.put("contents", contents);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        URI requestUri = UriComponentsBuilder.fromUriString(resolveGeminiEndpoint())
                .queryParam("key", apiKey)
                .build()
                .toUri();

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(requestUri, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody == null || !responseBody.containsKey("candidates")) {
                    throw new AiAnalysisException("No candidates returned from Gemini API");
                }

                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (candidates == null || candidates.isEmpty()) {
                    throw new AiAnalysisException("Empty candidates list returned from Gemini API");
                }

                Map<String, Object> firstCandidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                List<Map<String, String>> responseParts = (List<Map<String, String>>) content.get("parts");
                if (responseParts == null || responseParts.isEmpty()) {
                    throw new AiAnalysisException("No content parts returned from Gemini API");
                }
                return responseParts.get(0).get("text");
            }
            log.warn("Gemini analysis failed with status {}", response.getStatusCode());
            throw new AiAnalysisException("Gemini analysis failed with status " + response.getStatusCode());
        } catch (HttpStatusCodeException ex) {
            log.error("Gemini API rejected the request with status {} and body {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new AiAnalysisException("Gemini API request failed: " + extractGeminiErrorMessage(ex.getResponseBodyAsString()), ex);
        } catch (Exception ex) {
            if (ex instanceof AiAnalysisException) {
                throw (AiAnalysisException) ex;
            }
            log.error("Gemini integration failed", ex);
            throw new AiAnalysisException("Error in communicating with Google API service", ex);
        }
    }

    private String resolveGeminiEndpoint() {
        if (geminiEndpoint != null && !geminiEndpoint.isBlank()) {
            return geminiEndpoint;
        }
        return trimTrailingSlash(geminiBaseUrl) + "/models/" + geminiModel + ":generateContent";
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            throw new AiProviderConfigurationException("Gemini base URL is missing. Please configure GEMINI_BASE_URL.");
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String extractGeminiErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "no error details returned";
        }
        try {
            Map<String, Object> body = OBJECT_MAPPER.readValue(responseBody, new TypeReference<>() {
            });
            Object error = body.get("error");
            if (error instanceof Map<?, ?> errorMap) {
                Object message = errorMap.get("message");
                if (message != null) {
                    return message.toString();
                }
            }
        } catch (Exception ex) {
            log.debug("Unable to parse Gemini error response", ex);
        }
        return responseBody;
    }
}
