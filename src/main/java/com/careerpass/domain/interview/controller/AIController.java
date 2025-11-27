package com.careerpass.domain.interview.controller;

import com.careerpass.domain.interview.dto.AnswerUploadMetaDto;
import com.careerpass.domain.interview.dto.AnalysisResultDto;
import com.careerpass.domain.interview.service.AIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 🎧 STT 전용 컨트롤러
 * - 프론트(또는 다른 백엔드)에서 meta + file을 받아
 *   FastAPI(voice_ai.py)로 넘겨서 텍스트로 변환한 뒤 answerText만 반환한다.
 * - 파일 저장 / 인터뷰 메타데이터 관리는 InterviewController가 담당.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interview/voice")
@Slf4j
@Validated
public class AIController {

    private final AIService aiService;
    private final ObjectMapper objectMapper; // Swagger 에서 meta를 text/plain 으로 줄 때 방어용

    @Operation(summary = "STT 호출 api")
    @PostMapping(
            value = "/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AnalysisResultDto> analyze(
            @RequestPart("meta") String metaJson,   // Swagger에서 text로 보내도 받기 쉽게 String으로 받음
            @RequestPart("file") MultipartFile file
    ) {
        // 1) 파일 검증 (서비스에서도 한 번 더 검증)
        if (file == null || file.isEmpty()) {
            log.warn("빈 파일 업로드 요청. meta={}", metaJson);
            return ResponseEntity.badRequest().build();
        }

        // 2) meta JSON → DTO 파싱
        AnswerUploadMetaDto meta;
        try {
            meta = objectMapper.readValue(metaJson, AnswerUploadMetaDto.class);
        } catch (Exception e) {
            log.warn("meta 파싱 실패: {}", metaJson, e);
            return ResponseEntity.badRequest().build();
        }

        // 3) STT 호출
        AnalysisResultDto result = aiService.analyzeVoice(meta, file);
        if (result == null) {
            log.error("STT 결과 null 반환. meta={}", meta);
            return ResponseEntity.internalServerError().build();
        }

        // answerText 길이가 너무 길 수 있으니 앞부분만 로그에 찍기
        String snippet = result.getAnswerText();
        if (snippet != null && snippet.length() > 50) {
            snippet = snippet.substring(0, 50) + "...";
        }

        log.info("STT 완료: interviewId={}, questionId={}, textSnippet={}",
                meta.getInterviewId(), meta.getQuestionId(), snippet);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "정상 작동 확인 api")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }
}