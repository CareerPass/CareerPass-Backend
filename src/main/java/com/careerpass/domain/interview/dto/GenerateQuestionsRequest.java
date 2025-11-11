package com.careerpass.domain.interview.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 자소서 기반 질문 생성 요청 DTO
 */
@Builder
public record GenerateQuestionsRequest(
        @NotNull(message = "introductionId는 필수입니다.")
        Long introductionId,

        // 몇 개의 질문을 생성할지 (null이면 기본값 5)
        Integer count
) {}