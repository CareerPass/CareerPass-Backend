package com.careerpass.domain.interview.dto;

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
public record GenerateQuestionsResponse(
        String major,
        String jobTitle,
        OffsetDateTime generatedAt,
        List<QuestionItemDto> questions
) {}