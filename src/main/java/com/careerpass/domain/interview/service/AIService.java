package com.careerpass.domain.interview.service;

import com.careerpass.domain.interview.dto.VoiceAnalyzeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AIService {

    private final WebClient aiWebClient; // config에 등록한 Bean (baseUrl: http://localhost:5001)

    public VoiceAnalyzeResponse analyzeVoice(MultipartFile file) {
        // Multipart 전송: key 이름은 FastAPI에서 받는 "file" 과 동일해야 함
        return aiWebClient.post()
                .uri("/analyze")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", file.getResource()))
                .retrieve()
                .bodyToMono(VoiceAnalyzeResponse.class)
                .onErrorResume(ex ->
                        Mono.error(new RuntimeException("AI 서버 호출 실패: " + ex.getMessage(), ex)))
                .block();
    }
}