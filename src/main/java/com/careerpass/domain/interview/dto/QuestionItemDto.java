package com.careerpass.domain.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 🎯 질문 1개 단위를 표현하는 DTO
 * - questionId: 임시 또는 UUID 기반 식별자
 * - text: 실제 질문 텍스트
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "질문 항목")
public class QuestionItemDto {

    @Schema(example = "q-1", description = "질문 고유 ID")
    private String questionId;

    @Schema(example = "자기소개를 해주세요.", description = "질문 내용")
    private String text;
}