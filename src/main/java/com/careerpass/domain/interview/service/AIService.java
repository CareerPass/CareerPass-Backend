package com.careerpass.domain.interview.service;

import com.careerpass.domain.interview.dto.VoiceAnalyzeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AIService {

    private final WebClient aiWebClient; // config에 등록된 Bean (baseUrl: http://localhost:5001)

    // ✅ 비동기 방식 (Mono 반환)
    public Mono<VoiceAnalyzeResponse> analyzeVoice(MultipartFile file) {
        return aiWebClient.post()
                .uri("/analyze")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", file.getResource()))
                .retrieve()
                .bodyToMono(VoiceAnalyzeResponse.class)
                .onErrorResume(ex ->
                        Mono.error(new RuntimeException("AI 서버 호출 실패: " + ex.getMessage(), ex))
                );
    }
}