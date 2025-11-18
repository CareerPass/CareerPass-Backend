package com.careerpass.domain.interview.dto;

import lombok.Builder;

/**
 * 📨 모의면접 학습 기록 저장 요청 DTO
 *
 * - userId       : 답변한 사용자
 * - questionId   : 어떤 질문(Question)에 대한 답변인지
 * - audioUrl     : 저장된 음성 파일 경로 (S3, 로컬 등)
 * - answerText   : Whisper로 변환된 텍스트
 * - analysisResult : AI 피드백 / 평가 내용
 * - durationMs   : 답변 소요 시간 (ms)
 */
@Builder
public record SaveInterviewLearningRecordRequest(
        Long userId,
        Long questionId,
        String audioUrl,
        String answerText,
        String analysisResult,
        Long durationMs
) {}