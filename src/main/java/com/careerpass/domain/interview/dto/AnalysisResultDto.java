package com.careerpass.domain.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 🎧 STT 결과 DTO
 * - 음성을 텍스트로 변환한 결과만 담는다.
 * - 점수/피드백은 "다른 파이썬(팀원)"에서 처리 예정.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "음성 → 텍스트 변환 결과")
public class AnalysisResultDto {
    @Schema(description = "인터뷰 ID", example = "1")
    private Long interviewId;

    @Schema(description = "질문 ID", example = "q-1")
    private String questionId;

    @Schema(description = "사용자 ID", example = "10")
    private Long userId;

    @Schema(description = "Whisper로부터 받은 전사 텍스트")
    private String answerText;
}