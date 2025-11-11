package com.careerpass.domain.interview.service;

import com.careerpass.domain.interview.dto.AnswerUploadMetaDto;
import com.careerpass.domain.interview.dto.AnalysisResultDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.core.io.Resource;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final WebClient aiWebClient;     // config에 등록된 Bean (baseUrl: http://localhost:5001)
    private final ObjectMapper objectMapper; // meta(JSON) 직렬화에 사용

    /**
     * ✅ 동기 방식 (Controller가 DTO를 바로 받도록)
     * meta + file을 multipart/form-data로 FastAPI(/analyze)에 전달하고,
     * 응답을 AnalysisResultDto로 반환한다.
     */
    public AnalysisResultDto analyzeVoice(AnswerUploadMetaDto meta, MultipartFile file) {
        // 🚫 [예외1] 파일 비었을 때
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("❌ 업로드된 파일이 비어 있습니다.");
        }

        // 🚫 [예외2] 확장자 검증
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.matches(".*\\.(m4a|mp3|wav|webm|ogg)$")) {
            throw new IllegalArgumentException("❌ 지원하지 않는 파일 형식입니다. (허용: m4a, mp3, wav, webm, ogg)");
        }

        // ✅ meta를 JSON 문자열로 직렬화
        final String metaJson;
        try {
            metaJson = objectMapper.writeValueAsString(meta);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("❌ meta 직렬화 실패: " + e.getMessage(), e);
        }

        // ✅ multipart 파트 구성 (meta=JSON, file=리소스)
        MultiValueMap<String, Object> multipart = new LinkedMultiValueMap<>();
        multipart.add("meta", jsonPart(metaJson));
        multipart.add("file", filePart(file));

        try {
            // ✅ FastAPI 서버로 요청 (multipart/form-data)
            return aiWebClient.post()
                    .uri("/analyze")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(multipart))
                    .retrieve()
                    .bodyToMono(AnalysisResultDto.class)
                    .block(); // 동기 변환

        } catch (WebClientResponseException ex) {
            int status = ex.getStatusCode().value();
            String msg = switch (status) {
                case 400 -> "AI 서버에서 잘못된 요청을 받았습니다. (400)";
                case 413 -> "파일이 너무 큽니다. (최대 업로드 용량 초과)";
                case 500 -> "AI 서버 내부 오류 (500)";
                default -> "AI 서버 응답 오류 (" + status + ")";
            };
            log.error("AI analyze 실패: status={}, body={}", status, ex.getResponseBodyAsString());
            throw new RuntimeException("❌ " + msg, ex);

        } catch (Exception ex) {
            log.error("AI 서버 호출 실패: {}", ex.getMessage(), ex);
            throw new RuntimeException("❌ AI 서버 호출 실패: " + ex.getMessage(), ex);
        }
    }

    // ----------------------
    // 내부 유틸 (multipart 파트)
    // ----------------------

    private org.springframework.http.HttpEntity<String> jsonPart(String json) {
        var headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new org.springframework.http.HttpEntity<>(json, headers);
    }

    private org.springframework.http.HttpEntity<Resource> filePart(MultipartFile file) {
        var headers = new org.springframework.http.HttpHeaders();
        // 원본 contentType 없으면 이 정도로 넉넉하게
        headers.setContentType(file.getContentType() != null
                ? MediaType.parseMediaType(file.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("file", file.getOriginalFilename());
        return new org.springframework.http.HttpEntity<>(file.getResource(), headers);
    }
}