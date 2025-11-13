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

    @Schema(description = "변환된 답변 텍스트", example = "저는 백엔드 개발자로서 ...")
    private String answerText;
}