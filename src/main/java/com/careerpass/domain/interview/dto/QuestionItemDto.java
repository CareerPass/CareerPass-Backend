package com.careerpass.domain.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🎯 질문 1개 단위를 표현하는 DTO
 * - questionId: 고유 식별자(UUID 등)
 * - text: 질문 내용
 * - category: 질문 유형(예: intro, project, motivation 등)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "질문 항목")
public class QuestionItemDto {
    @Schema(example = "q-intro") private String questionId;
    @Schema(example = "자기소개를 해주세요.") private String text;
    @Schema(example = "intro") private String category; // intro / technical / behavior
}
