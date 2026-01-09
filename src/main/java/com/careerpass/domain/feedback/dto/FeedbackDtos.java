package com.careerpass.domain.feedback.dto;

import com.careerpass.domain.feedback.entity.FeedbackType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FeedbackDtos {

    // 생성 요청
    public record CreateRequest(
            @NotNull Long userId,
            String title,
            @NotNull FeedbackType feedbackType,
            @NotNull Long totalScore,
            String transcript,
            @NotBlank String feedbackText,
            @NotBlank String sectionFeedback,
            Long introductionId,   // INTRODUCTION일 때만 값
            Long interviewId,      // INTERVIEW일 때만 값
            Long questionId,
            String audioUrl,
            Long durationMs
    ) {}

    public record UpdateTitleRequest(
            @NotBlank
            @Size(max = 50)
            String title
    ) {}

    public record TitleResponse(
            Long id,
            String title
    ) {}

    // 응답
    public record Response(
            Long id,
            Long userId,
            String title,
            FeedbackType feedbackType,
            Long totalScore,
            String transcript,
            String feedbackText,
            String sectionFeedback,
            Long introductionId,
            Long interviewId,
            Long questionId,
            String audioUrl,
            Long durationMs,
            java.time.LocalDateTime createdAt
    ) {}

    // 목록 요약 응답
    public record SummaryResponse(
            Long id,
            String title,
            FeedbackType feedbackType,
            Long totalScore,
            java.time.LocalDateTime createdAt
    ) {}
}
