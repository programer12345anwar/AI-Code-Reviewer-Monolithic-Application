package com.anwar.aicodereview.service;

import org.springframework.stereotype.Service;

@Service
public class AIService extends AiAnalysisService {

    public AIService(org.springframework.web.client.RestTemplate restTemplate) {
        super(restTemplate);
    }
}
