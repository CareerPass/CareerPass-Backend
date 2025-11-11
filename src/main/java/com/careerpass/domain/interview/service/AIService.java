package com.careerpass.domain.interview.service;

import com.careerpass.domain.interview.dto.AnswerUploadMetaDto;
import com.careerpass.domain.interview.dto.AnalysisResultDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private static final Set<String> ALLOWED_EXT = Set.of("m4a", "mp3", "wav", "webm", "ogg");
    private static final long MAX_BYTES = 25L * 1024 * 1024; // 25MB (서버/역량 맞춰 조절)

    private final WebClient aiWebClient;     // baseUrl은 config에서 주입
    private final ObjectMapper objectMapper; // meta(JSON) 직렬화

    /**
     * meta + file을 multipart/form-data로 FastAPI(/analyze)에 전달하고
     * 응답을 AnalysisResultDto로 동기 반환.
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

        // 3) meta 직렬화
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
            // 5) 호출 (타임아웃 + 에러 응답 맵핑)
            Mono<AnalysisResultDto> mono = aiWebClient.post()
                    .uri("/analyze")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(multipart))
                    .retrieve()
                    .onStatus(s -> s.is4xxClientError(), cr ->
                            cr.bodyToMono(String.class).defaultIfEmpty("")
                                    .map(body -> new IllegalArgumentException("AI 서버 4xx 응답: " + body)))
                    .onStatus(s -> s.is5xxServerError(), cr ->
                            cr.bodyToMono(String.class).defaultIfEmpty("")
                                    .map(body -> new IllegalStateException("AI 서버 5xx 응답: " + body)))
                    .bodyToMono(AnalysisResultDto.class)
                    .timeout(Duration.ofSeconds(30));

            AnalysisResultDto dto = mono.block(); // 동기 변환

            if (dto == null || dto.getQuestionId() == null || dto.getAnswerText() == null || dto.getScore() == null) {
                throw new IllegalStateException("AI 응답이 불완전합니다.");
            }
            return dto;

        } catch (WebClientResponseException ex) {
            log.error("AI analyze 실패: status={}, body={}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new IllegalStateException("AI 서버 응답 오류: " + ex.getRawStatusCode(), ex);

        } catch (Exception ex) {
            log.error("AI 서버 호출 실패: {}", ex.getMessage(), ex);
            throw new IllegalStateException("AI 서버 호출 실패: " + ex.getMessage(), ex);
        }
    }

    // ---------- 내부 유틸 ----------

    private org.springframework.http.HttpEntity<String> jsonPart(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new org.springframework.http.HttpEntity<>(json, headers);
    }

    private org.springframework.http.HttpEntity<org.springframework.core.io.Resource> filePart(MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(file.getContentType() != null
                ? MediaType.parseMediaType(file.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("file", file.getOriginalFilename());
        return new org.springframework.http.HttpEntity<>(file.getResource(), headers);
    }

    private String extractExt(String filename) {
        if (filename == null) return null;
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return null;
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}