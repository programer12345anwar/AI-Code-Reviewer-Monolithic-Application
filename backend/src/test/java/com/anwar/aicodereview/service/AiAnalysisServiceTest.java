package com.anwar.aicodereview.service;

import com.anwar.aicodereview.exception.AiAnalysisException;
import com.anwar.aicodereview.exception.AiProviderConfigurationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AiAnalysisServiceTest {

    @Test
    void analyzeCodeShouldFailFastWhenGoogleApiKeyIsMissing() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        AiAnalysisService aiAnalysisService = new AiAnalysisService(restTemplate);
        ReflectionTestUtils.setField(aiAnalysisService, "apiKey", " ");

        AiProviderConfigurationException exception = assertThrows(
                AiProviderConfigurationException.class,
                () -> aiAnalysisService.analyzeCode("class Calculator {}")
        );

        assertEquals("Google API Key is missing. Please configure GOOGLE_API_KEY.", exception.getMessage());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void analyzeCodeShouldCallGeminiRestEndpointWithApiKeyQueryParam() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        AiAnalysisService aiAnalysisService = new AiAnalysisService(restTemplate);
        ReflectionTestUtils.setField(aiAnalysisService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(
                aiAnalysisService,
                "geminiBaseUrl",
                "https://generativelanguage.googleapis.com/v1beta"
        );
        ReflectionTestUtils.setField(
                aiAnalysisService,
                "geminiModel",
                "gemini-3.6-flash"
        );
        ReflectionTestUtils.setField(aiAnalysisService, "geminiEndpoint", "");

        JsonNode responseBody = new ObjectMapper().valueToTree(Map.of(
                "candidates",
                List.of(Map.of(
                        "content",
                        Map.of("parts", List.of(Map.of("text", "analysis result")))
                ))
        ));

        when(restTemplate.postForObject(any(URI.class), any(HttpEntity.class), eq(JsonNode.class)))
                .thenReturn(responseBody);

        String result = aiAnalysisService.analyzeCode("class Calculator {}");

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).postForObject(uriCaptor.capture(), any(HttpEntity.class), eq(JsonNode.class));
        assertEquals("analysis result", result);
        assertEquals(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=test-api-key",
                uriCaptor.getValue().toString()
        );
    }

    @Test
    void analyzeCodeShouldUseEndpointOverrideWhenConfigured() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        AiAnalysisService aiAnalysisService = new AiAnalysisService(restTemplate);
        ReflectionTestUtils.setField(aiAnalysisService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(
                aiAnalysisService,
                "geminiEndpoint",
                "https://example.test/custom-gemini:generateContent"
        );

        JsonNode responseBody = new ObjectMapper().valueToTree(Map.of(
                "candidates",
                List.of(Map.of(
                        "content",
                        Map.of("parts", List.of(Map.of("text", "analysis result")))
                ))
        ));

        when(restTemplate.postForObject(any(URI.class), any(HttpEntity.class), eq(JsonNode.class)))
                .thenReturn(responseBody);

        aiAnalysisService.analyzeCode("class Calculator {}");

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).postForObject(uriCaptor.capture(), any(HttpEntity.class), eq(JsonNode.class));
        assertEquals(
                "https://example.test/custom-gemini:generateContent?key=test-api-key",
                uriCaptor.getValue().toString()
        );
    }

    @Test
    void analyzeCodeShouldRejectMalformedProviderResponses() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        AiAnalysisService aiAnalysisService = new AiAnalysisService(restTemplate);
        ReflectionTestUtils.setField(aiAnalysisService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(aiAnalysisService, "geminiEndpoint", "https://example.test/generate");
        JsonNode responseBody = new ObjectMapper().createObjectNode().set("candidates",
                new ObjectMapper().createArrayNode());
        when(restTemplate.postForObject(any(URI.class), any(HttpEntity.class), eq(JsonNode.class)))
                .thenReturn(responseBody);

        AiAnalysisException exception = assertThrows(AiAnalysisException.class,
                () -> aiAnalysisService.analyzeCode("class Example {}"));

        assertEquals("AI provider returned no analysis candidates", exception.getMessage());
    }

    @Test
    void analyzeCodeShouldReturnSafeMessageOnProviderTimeout() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        AiAnalysisService aiAnalysisService = new AiAnalysisService(restTemplate);
        ReflectionTestUtils.setField(aiAnalysisService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(aiAnalysisService, "geminiEndpoint", "https://example.test/generate");
        when(restTemplate.postForObject(any(URI.class), any(HttpEntity.class), eq(JsonNode.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        AiAnalysisException exception = assertThrows(AiAnalysisException.class,
                () -> aiAnalysisService.analyzeCode("class Example {}"));

        assertEquals("Unable to communicate with the AI provider", exception.getMessage());
    }
}
