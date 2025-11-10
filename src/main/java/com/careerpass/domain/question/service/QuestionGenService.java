package com.careerpass.domain.question.service;

import com.careerpass.domain.question.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class QuestionGenService {

    // config.WebClientConfig에서 등록한 Bean
    private final WebClient questionGenWebClient;

    /**
     * 자기소개서 맥락(학과/직무)으로 면접 질문 생성 요청
     * - 자바에서 camelCase(jobTitle) → Flask는 snake_case(job_title)로 변환해서 전달
     */
    public Mono<GenerateQuestionsResponse> generate(String major, String jobTitle) {
        FlaskQuestionRequest flaskReq = new FlaskQuestionRequest(major, jobTitle);

        return questionGenWebClient.post()
                .uri("/api/questions")                    // Flask 엔드포인트
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(flaskReq)                      // JSON 바디
                .retrieve()
                .bodyToMono(FlaskQuestionResponse.class)  // Flask 응답 → DTO
                .map(res -> new GenerateQuestionsResponse(res.questions()))
                .onErrorResume(ex ->
                        Mono.error(new RuntimeException("질문 생성기 호출 실패: " + ex.getMessage(), ex))
                );
    }
}