package com.careerpass.domain.interview.controller;

import com.careerpass.domain.interview.dto.AnswerUploadMetaDto;
import com.careerpass.domain.interview.dto.AnalysisResultDto;
import com.careerpass.domain.interview.service.AIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 🤖 AI 분석 전용 컨트롤러
 * - 파일을 받아 FastAPI로 전송하고 분석 결과(전사+점수+피드백)를 반환
 * - 저장/DB관리는 InterviewController가 담당
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interview/voice")
@Slf4j
@Validated
public class AIController {

    private final AIService aiService;
    private final ObjectMapper objectMapper; // ✅ Swagger text/plain meta 방어용

    @PostMapping(
            value = "/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AnalysisResultDto> analyze(
            @RequestPart("meta") String metaJson,   // ✅ String으로 받기
            @RequestPart("file") MultipartFile file
    ) {
        // 컨트롤러 레벨 최소 방어 (서비스에서도 재검증)
        if (file == null || file.isEmpty()) {
            log.warn("빈 파일 업로드 요청. meta={}", metaJson);
            return ResponseEntity.badRequest().build();
        }

        // ✅ meta JSON 파싱
        AnswerUploadMetaDto meta;
        try {
            meta = objectMapper.readValue(metaJson, AnswerUploadMetaDto.class);
        } catch (Exception e) {
            log.warn("meta 파싱 실패: {}", metaJson, e);
            return ResponseEntity.badRequest().build();
        }

        AnalysisResultDto result = aiService.analyzeVoice(meta, file);
        if (result == null) {
            log.error("AI 분석 결과 null 반환. meta={}", meta);
            return ResponseEntity.internalServerError().build();
        }

        log.info("AI 분석 완료: interviewId={}, questionId={}, score={}",
                meta.getInterviewId(), meta.getQuestionId(), result.getScore());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }
}