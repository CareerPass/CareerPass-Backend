package com.careerpass.domain.feedback.dto;

import com.careerpass.domain.feedback.entity.FeedbackType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FeedbackDtos {

    // 생성 요청
    public record CreateRequest(
            @NotNull FeedbackType feedbackType,
            @NotNull Long totalScore,
            @NotBlank String feedbackText,
            @NotBlank String sectionFeedback,
            Long introductionId,   // INTRODUCTION일 때만 값
            Long interviewId       // INTERVIEW일 때만 값
    ) {}

    // 응답
    public record Response(
            Long id,
            FeedbackType feedbackType,
            Long totalScore,
            String feedbackText,
            String sectionFeedback,
            Long introductionId,
            Long interviewId
    ) {}
}