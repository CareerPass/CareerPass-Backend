package com.careerpass.domain.interview.controller;

import com.careerpass.domain.interview.dto.AnswerUploadMetaDto;
import com.careerpass.domain.interview.dto.AnalysisResultDto;
import com.careerpass.domain.interview.service.AIService;
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
            @RequestPart("meta") AnswerUploadMetaDto meta,
            @RequestPart("file") MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 서비스는 meta+file을 받아 전사→분석까지 처리하고 결과 DTO를 반환
        AnalysisResultDto result = aiService.analyzeVoice(meta, file);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }
}