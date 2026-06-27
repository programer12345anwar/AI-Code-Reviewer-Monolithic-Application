package com.anwar.aicodereview.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Code Reviewer API")
                        .description("REST API for code submission, versioning, and AI-powered code analysis")
                        .version("v1"));
    }
}
