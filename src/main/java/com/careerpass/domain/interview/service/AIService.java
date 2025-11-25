package com.careerpass.domain.interview.service;

import com.careerpass.domain.interview.dto.AnswerUploadMetaDto;
import com.careerpass.domain.interview.dto.AnalysisResultDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Locale;
import java.util.Set;

/**
 * 🎧 STT(AI 음성 → 텍스트) 호출 서비스
 * - meta + file을 FastAPI(voice_ai.py)의 /analyze 에 전달
 * - 변환된 텍스트(answerText)만 돌려받는다.
 * - 점수/피드백 분석은 다른 Python 서버에서 처리 예정.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    // 허용 확장자
    private static final Set<String> ALLOWED_EXT = Set.of("m4a", "mp3", "wav", "webm", "ogg");
    // 최대 파일 크기 (25MB)
    private static final long MAX_BYTES = 25L * 1024 * 1024;

    // WebClientConfig 에서 baseUrl("http://localhost:5001")로 설정해둔 Bean
    private final WebClient aiWebClient;

    // meta(JSON) 직렬화용
    private final ObjectMapper objectMapper;

    /**
     * 🎯 meta + file을 multipart/form-data로 FastAPI(/analyze)에 전달하고,
     *     STT 결과(텍스트만)를 동기 방식으로 반환한다.
     */
    public AnalysisResultDto analyzeVoice(AnswerUploadMetaDto meta, MultipartFile file) {
        // 1) 기본 검증
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 비어 있습니다.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("파일이 너무 큽니다. 최대 " + (MAX_BYTES / (1024 * 1024)) + "MB 까지 허용합니다.");
        }

        // 2) 확장자 검증 (케이스 무시)
        final String filename = file.getOriginalFilename();
        final String ext = extractExt(filename);
        if (ext == null || !ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. 허용: " + ALLOWED_EXT);
        }

        // 3) meta 직렬화 (JSON 문자열)
        final String metaJson;
        try {
            metaJson = objectMapper.writeValueAsString(meta);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("meta 직렬화 실패: " + e.getOriginalMessage(), e);
        }

        // 4) multipart 구성
        MultiValueMap<String, Object> multipart = new LinkedMultiValueMap<>();
        multipart.add("meta", jsonPart(metaJson));
        multipart.add("file", filePart(file));

        try {
            // 5) FastAPI(/analyze) 호출
            Mono<SttResponse> mono = aiWebClient.post()
                    .uri("/voice/analyze")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(multipart))
                    .retrieve()
                    .onStatus(s -> s.is4xxClientError(), cr ->
                            cr.bodyToMono(String.class).defaultIfEmpty("")
                                    .map(body -> new IllegalArgumentException("STT 서버 4xx 응답: " + body)))
                    .onStatus(s -> s.is5xxServerError(), cr ->
                            cr.bodyToMono(String.class).defaultIfEmpty("")
                                    .map(body -> new IllegalStateException("STT 서버 5xx 응답: " + body)))
                    .bodyToMono(SttResponse.class)
                    .timeout(Duration.ofSeconds(30));

            // 동기(block) 변환
            SttResponse stt = mono.block();

            // 6) 응답 검증: answerText만 있으면 된다
            if (stt == null || stt.answerText() == null || stt.answerText().isBlank()) {
                throw new IllegalStateException("STT 응답이 비어 있습니다.");
            }

            // 7) meta + STT 텍스트를 합쳐서 우리 DTO로 래핑
            return AnalysisResultDto.builder()
                    .interviewId(meta.getInterviewId())
                    .questionId(meta.getQuestionId())
                    .answerText(stt.answerText())
                    .build();

        } catch (WebClientResponseException ex) {
            log.error("STT analyze 실패: status={}, body={}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new IllegalStateException("STT 서버 응답 오류: " + ex.getRawStatusCode(), ex);

        } catch (Exception ex) {
            log.error("STT 서버 호출 실패: {}", ex.getMessage(), ex);
            throw new IllegalStateException("STT 서버 호출 실패: " + ex.getMessage(), ex);
        }
    }

    // ---------- 내부 유틸 ----------

    /** meta JSON 파트 */
    private org.springframework.http.HttpEntity<String> jsonPart(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new org.springframework.http.HttpEntity<>(json, headers);
    }

    /** 파일 파트 */
    private org.springframework.http.HttpEntity<org.springframework.core.io.Resource> filePart(MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(file.getContentType() != null
                ? MediaType.parseMediaType(file.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("file", file.getOriginalFilename());
        return new org.springframework.http.HttpEntity<>(file.getResource(), headers);
    }

    /** 파일 확장자 추출 (소문자) */
    private String extractExt(String filename) {
        if (filename == null) return null;
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return null;
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * 🔹 FastAPI STT 서버에서 오는 JSON 형식
     * { "answerText": "..." }
     */
    private record SttResponse(String answerText) {}
}