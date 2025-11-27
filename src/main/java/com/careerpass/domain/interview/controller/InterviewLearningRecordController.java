package com.careerpass.domain.interview.controller;

import com.careerpass.domain.interview.dto.SaveInterviewLearningRecordRequest;
import com.careerpass.domain.interview.entity.InterviewLearningRecord;
import com.careerpass.domain.interview.service.InterviewLearningRecordService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 🎤 모의면접 학습 기록 관련 API 컨트롤러
 *
 * - 질문별 음성 답변 / 분석 결과를 저장하는 역할
 */
@RestController
@RequestMapping("/api/interview-learning")
@RequiredArgsConstructor
public class InterviewLearningRecordController {

    private final InterviewLearningRecordService learningRecordService;

    /**
     * 🔹 모의면접 학습 기록 저장 API
     *
     * [POST] /api/interview-learning
     *
     * RequestBody:
     * {
     *   "userId": 1,
     *   "questionId": 3,
     *   "audioUrl": "https://s3.../audio1.wav",
     *   "answerText": "제가 지원한 이유는...",
     *   "analysisResult": "논리 구조는 좋지만, 예시가 부족합니다.",
     *   "durationMs": 21500
     * }
     */
    @Operation(summary = "모의 면접 피드백 결과 저장 api")
    @PostMapping
    public ResponseEntity<InterviewLearningRecord> saveRecord(
            @RequestBody SaveInterviewLearningRecordRequest request
    ) {
        // 서비스 호출해 학습 기록 저장
        InterviewLearningRecord saved = learningRecordService.saveRecord(
                request.userId(),
                request.questionId(),
                request.audioUrl(),
                request.answerText(),
                request.analysisResult(),
                request.durationMs()
        );

        // 저장된 결과 반환 (필요 시 DTO로 변환도 가능)
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}