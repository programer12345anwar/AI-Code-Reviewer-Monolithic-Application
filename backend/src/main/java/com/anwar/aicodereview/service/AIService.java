package com.anwar.aicodereview.service;

import org.springframework.web.client.RestTemplate;

/**
 * Backward-compatible alias for older code that referenced AIService directly.
 */
@Deprecated
public class AIService extends AiAnalysisService {

    public AIService(RestTemplate restTemplate) {
        super(restTemplate);
    }
}
