package com.careerpass.domain.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 🎯 면접 질문 생성 응답 DTO
 * - major/jobTitle: 요청 시 전달받은 전공 및 직무
 * - generatedAt: 생성 시각
 * - questions: 생성된 질문 리스트
 */
@Builder
@Schema(description = "면접 질문 생성 응답")
public record GenerateQuestionsResponse(

        @Schema(description = "지원 전공 또는 학과명", example = "컴퓨터공학과")
        String major,

        @Schema(description = "지원 직무명", example = "백엔드 개발자")
        String jobTitle,

        @Schema(description = "질문이 생성된 시각 (UTC 기준)", example = "2025-11-12T12:34:56Z")
        OffsetDateTime generatedAt,

        @Schema(description = "생성된 면접 질문 리스트")
        List<QuestionItemDto> questions
) {}