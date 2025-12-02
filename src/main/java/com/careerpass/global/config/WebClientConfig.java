package com.careerpass.global.config;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    /**
     * ✅ FastAPI (음성 분석 서버)와 통신할 WebClient Bean
     * - 기본 주소: http://localhost:5001
     * - AIService에서 주입받아 사용
     */
    @Bean
    public WebClient aiWebClient() {
        HttpClient httpClient = HttpClient.create()
                // 전체 응답 타임아웃 설정: 60초
                .responseTimeout(Duration.ofSeconds(60))

                // Netty 핸들러를 통한 상세 읽기/쓰기 타임아웃 설정: 60초
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(60, TimeUnit.SECONDS)));
        return WebClient.builder()
                .baseUrl("http://13.125.192.47:8088")  // FastAPI 서버 주소
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    /**
     * ✅ Flask (면접 질문 생성 서버)와 통신할 WebClient Bean
     * - 기본 주소: http://localhost:5002
     * - InterviewQuestionService 또는 별도 AI 모듈에서 주입받아 사용
     */

    /*
    @Bean
    public WebClient questionGenWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:5002")  // 필요 시 application.yml로 분리 가능
                .build();
    }
     */
}