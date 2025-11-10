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

    // ✅ 면접 질문 생성 Flask 서버(기본: http://localhost:5000) 호출 전용 WebClient
    @Bean
    public WebClient questionGenWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:5000") // 필요하면 yml로 뺄 수 있음
                .build();
    }
}