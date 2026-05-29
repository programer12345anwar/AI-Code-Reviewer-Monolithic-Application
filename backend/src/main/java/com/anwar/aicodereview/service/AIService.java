package com.anwar.aicodereview.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${google.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";

    public String analyzeCode(String code) {

        if(apiKey == null || apiKey.isEmpty()) {
            return "Error: Google API Key is missing. Please set the GOOGLE_API_KEY environment variable";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-goog-api-key", apiKey);

        String promptText = "You are a senior software engineer. Analyze the following code:\n - Identify bugs\n -Suggest improvements\n - Suggest optimised code version\n -Indicate potential security issues\n\n Code:\n"
                + code;

        // Gemini JSON Request
        // { "contents": [ { "parts": [{"text": "How does AI work?" }] } ] }

        Map<String, Object> body = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> contentPart = new HashMap<>();
        List<Map<String, String>> parts = new ArrayList<>();
        Map<String, String> testPart = new HashMap<>();

        testPart.put("text", promptText);
        parts.add(testPart);
        contentPart.put("parts", parts);
        contents.add(contentPart);
        body.put("contents", contents);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(GEMINI_URL, request, Map.class);

            if(response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();

                // Gemini JSON Response
                // {"candidates": [{"content": [ { "parts": [{"text": "..."}]}}]}
                if(responseBody == null || !responseBody.containsKey("candidates")) {
                    return "Error: No candidates return from Gemini API";
                }

                List<Map<String, Object>> candidates = (List<Map<String, Object>> )responseBody.get("candidates");
                if(candidates.isEmpty()){
                    return "Error: Empty candidates list";
                }

                Map<String, Object> firstCandidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                List<Map<String, String>> responseParts = (List<Map<String, String>>) content.get("parts");

                if(responseParts.isEmpty()) {
                    return "Error: No content parts in the response";
                }

                return (String) responseParts.get(0).get("text");

            }
            else {
                return "Error analyzing code: " + response.getStatusCode();
            }
        }
        catch (Exception e) {
            return "Error in communicating with Google API service: " + e.getMessage();
        }

    }
}
