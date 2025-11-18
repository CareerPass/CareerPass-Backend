package com.careerpass.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /**
     * ✅ FastAPI (음성 분석 서버)와 통신할 WebClient Bean
     * - 기본 주소: http://localhost:5001
     * - AIService에서 주입받아 사용
     */
    @Bean
    public WebClient aiWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:5001")  // FastAPI 서버 주소
                .build();
    }

    /**
     * ✅ Flask (면접 질문 생성 서버)와 통신할 WebClient Bean
     * - 기본 주소: http://localhost:5002
     * - InterviewQuestionService 또는 별도 AI 모듈에서 주입받아 사용
     */
    @Bean
    public WebClient questionGenWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:5002")  // 필요 시 application.yml로 분리 가능
                .build();
    }
}