package com.careerpass.domain.interview.dto;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 자소서 기반 생성된 질문 목록 응답 DTO
 */
@Builder
public record GenerateQuestionsResponse(
        Long introductionId,
        OffsetDateTime generatedAt,
        List<QuestionItemDto> questions
) {}