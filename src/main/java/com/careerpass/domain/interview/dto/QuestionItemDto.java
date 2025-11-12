package com.careerpass.domain.interview.dto;

import lombok.AllArgsConstructor;
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
public class QuestionItemDto {
    private String questionId;
    private String text;
    private String category;
}