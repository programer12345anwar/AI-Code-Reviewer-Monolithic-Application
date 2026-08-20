package com.anwar.aicodereview.service;

import com.anwar.aicodereview.exception.AiProviderConfigurationException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
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

        Map<String, Object> responseBody = Map.of(
                "candidates",
                List.of(Map.of(
                        "content",
                        Map.of("parts", List.of(Map.of("text", "analysis result")))
                ))
        );

        when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        String result = aiAnalysisService.analyzeCode("class Calculator {}");

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).postForEntity(uriCaptor.capture(), any(HttpEntity.class), eq(Map.class));
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

        Map<String, Object> responseBody = Map.of(
                "candidates",
                List.of(Map.of(
                        "content",
                        Map.of("parts", List.of(Map.of("text", "analysis result")))
                ))
        );

        when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        aiAnalysisService.analyzeCode("class Calculator {}");

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).postForEntity(uriCaptor.capture(), any(HttpEntity.class), eq(Map.class));
        assertEquals(
                "https://example.test/custom-gemini:generateContent?key=test-api-key",
                uriCaptor.getValue().toString()
        );
    }
}
