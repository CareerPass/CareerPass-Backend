package com.careerpass.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // ✅ FastAPI 서버와 통신할 WebClient Bean 등록
    @Bean
    public WebClient aiWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:5001")  // FastAPI 서버 주소
                .build();
    }
}