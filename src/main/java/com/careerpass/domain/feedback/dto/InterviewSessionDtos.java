package com.careerpass.domain.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class InterviewSessionDtos {

    public record InterviewSessionSummaryDto(
            Long interviewId,
            LocalDateTime createdAt,
            String title,
            String jobApplied,
            Long totalDurationSec,
            Integer questionCount,
            Double averageScore
    ) {}

    public record InterviewSessionSummaryWithOverallDto(
            Long interviewId,
            LocalDateTime createdAt,
            String title,
            String jobApplied,
            Long totalDurationSec,
            Integer questionCount,
            Double averageScore,
            List<String> overallStrengths,
            List<String> overallImprovements,
            List<String> overallRisks
    ) {}

    public record InterviewAnswerDto(
            Long questionId,
            String questionText,
            String transcript,
            Integer score,
            Long timeMs,
            String audioUrl,
            List<String> strengths,
            List<String> improvements,
            List<String> risks
    ) {}

    public record InterviewResultSheetDto(
            InterviewSessionSummaryWithOverallDto summary,
            List<InterviewAnswerDto> answers
    ) {}

    public record FinalizeRequest(
            @NotNull Long interviewId,
            Long totalDurationSec
    ) {}

    public record UpdateTitleRequest(
            @NotBlank String title
    ) {}

    public record TitleResponse(
            Long interviewId,
            String title
    ) {}

    public record StartRequest(
            @NotNull String jobApplied,
            String resumeContent
    ) {}

    public record StartResponse(
            Long interviewId
    ) {}
}
