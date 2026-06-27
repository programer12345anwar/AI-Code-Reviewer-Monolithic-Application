package com.anwar.aicodereview.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisService.class);

    private final RestTemplate restTemplate;

    @Value("${ai.gemini.api-key:}")
    private String apiKey;

    @Value("${ai.gemini.endpoint:https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent}")
    private String geminiEndpoint;

    public AiAnalysisService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String analyzeCode(String code) {
        if (apiKey == null || apiKey.isBlank()) {
            return "Error: Google API Key is missing. Please configure GOOGLE_API_KEY.";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-goog-api-key", apiKey);

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

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(geminiEndpoint, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody == null || !responseBody.containsKey("candidates")) {
                    return "Error: No candidates returned from Gemini API";
                }

                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (candidates == null || candidates.isEmpty()) {
                    return "Error: Empty candidates list";
                }

                Map<String, Object> firstCandidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                List<Map<String, String>> responseParts = (List<Map<String, String>>) content.get("parts");
                if (responseParts == null || responseParts.isEmpty()) {
                    return "Error: No content parts in the response";
                }
                return responseParts.get(0).get("text");
            }
            log.warn("Gemini analysis failed with status {}", response.getStatusCode());
            return "Error analyzing code: " + response.getStatusCode();
        } catch (Exception ex) {
            log.error("Gemini integration failed", ex);
            return "Error in communicating with Google API service: " + ex.getMessage();
        }
    }
}
