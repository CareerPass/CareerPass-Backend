package com.careerpass.domain.interview.dto;

import lombok.*;

/**
 * 🤖 AI 음성 분석 결과 DTO
 * - Whisper/OpenAI 등 외부 AI 분석 서버의 응답을 받아
 *   프론트로 전달하기 위한 데이터 구조
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResultDto {

    /** 분석된 질문 ID */
    private String questionId;

    /** 전사된 답변 텍스트 */
    private String answerText;

    /** 답변의 종합 점수 (0~100 등) */
    private Double score;

    /** AI가 제공한 피드백 문장 */
    private String feedback;

    /** 답변 길이 (초 단위) */
    private Double durationSec;
}