package com.careerpass.domain.interview.controller;

import com.careerpass.domain.interview.dto.AnswerUploadMetaDto;
import com.careerpass.domain.interview.dto.AnalysisResultDto;
import com.careerpass.domain.interview.service.AIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interview/voice")
@Slf4j
@Validated
public class AIController {

    private final AIService aiService;

    /**
     * 🎧 음성 분석(전사+평가 통합) 엔드포인트
     * - multipart/form-data 로 meta(JSON) + file(audio/*) 수신
     * - meta: { interviewId, questionId }  (AnswerUploadMetaDto)
     * - 응답: AnalysisResultDto (점수/피드백 등)
     */
    @PostMapping(
            value = "/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AnalysisResultDto> analyze(
            @Valid @RequestPart("meta") AnswerUploadMetaDto meta,
            @RequestPart("file") MultipartFile file
    ) {
        // 🔸 1. 파일 기본 검증 (서비스에서 예외 던지지만 이중 방어)
        if (file == null || file.isEmpty()) {
            log.warn("파일이 비어 있음 (meta={})", meta);
            return ResponseEntity.badRequest().build();
        }

        // 🔸 2. 분석 실행
        AnalysisResultDto result = aiService.analyzeVoice(meta, file);

        // 🔸 3. 결과 검증 (예외 처리와 분리)
        if (result == null) {
            log.error("AI 분석 결과가 null 반환됨 (meta={})", meta);
            return ResponseEntity.internalServerError().build();
        }

        log.info("AI 분석 완료: interviewId={}, questionId={}, score={}",
                meta.getInterviewId(), meta.getQuestionId(), result.getScore());

        return ResponseEntity.ok(result);
    }

    /**
     * ✅ AI 서버 연동 헬스체크
     * 프론트/백 분리 배포 시 CORS/연결 확인용
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }
}