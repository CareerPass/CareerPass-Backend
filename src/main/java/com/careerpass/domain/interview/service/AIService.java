package com.careerpass.domain.interview.service;

import com.careerpass.domain.interview.dto.VoiceAnalyzeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AIService {

    private final WebClient aiWebClient; // config에 등록된 Bean (baseUrl: http://localhost:5001)

    // ✅ 비동기 방식 (Mono 반환)
    public Mono<VoiceAnalyzeResponse> analyzeVoice(MultipartFile file) {

        // 🚫 [예외1] 파일 비었을 때
        if (file == null || file.isEmpty()) {
            return Mono.error(new IllegalArgumentException("❌ 업로드된 파일이 비어 있습니다."));
        }

        // 🚫 [예외2] 확장자 검증
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.matches(".*\\.(m4a|mp3|wav)$")) {
            return Mono.error(new IllegalArgumentException("❌ 지원하지 않는 파일 형식입니다. (허용: m4a, mp3, wav)"));
        }

        // ✅ Whisper FastAPI 서버로 요청
        return aiWebClient.post()
                .uri("/analyze")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", file.getResource()))
                .retrieve()
                .bodyToMono(VoiceAnalyzeResponse.class)
                // ⚠️ FastAPI 서버가 꺼졌거나 500 반환 시 처리
                .onErrorResume(WebClientResponseException.class, ex -> {
                    int status = ex.getStatusCode().value();
                    String msg = switch (status) {
                        case 400 -> "AI 서버에서 잘못된 요청을 받았습니다. (400)";
                        case 413 -> "파일이 너무 큽니다. (최대 20MB)";
                        case 500 -> "AI 서버 내부 오류 (500)";
                        default -> "AI 서버 응답 오류 (" + status + ")";
                    };
                    return Mono.error(new RuntimeException("❌ " + msg));
                })
                // ⚠️ 그 외 네트워크/타임아웃 등 일반 오류 처리
                .onErrorResume(ex ->
                        Mono.error(new RuntimeException("❌ AI 서버 호출 실패: " + ex.getMessage(), ex))
                );
    }
}