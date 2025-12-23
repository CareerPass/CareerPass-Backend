package com.careerpass.domain.feedback.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * 자기소개서(이력서) AI 피드백용 DTO
 */
public class IntroductionAiDtos {

    /**
     * 클라이언트 → 스프링 요청 DTO (userId는 Principal에서 주입)
     */
    public record IntroFeedbackRequest(
            @NotBlank(message = "자기소개서(이력서) 내용은 필수입니다.")
            String resumeContent   // 🔴 파이썬의 ResumeInput.resumeContent 와 동일
    ) {}

    /**
     * 스프링 → 파이썬 요청 DTO (Principal userId를 합쳐 전달)
     */
    public record IntroFeedbackDispatch(
            Long userId,
            String resumeContent
    ) {}

    /**
     * 파이썬 → 스프링 응답 DTO
     * (FeedbackResponse와 동일 구조)
     */
    public record IntroFeedbackResponse(
            Long userId,
            String feedback,
            @JsonProperty("original_resume")
            String originalResume,
            @JsonProperty("regen_resume")
            String regenResume,
            @JsonProperty("regen_toss_resume")
            String regenTossResume
    ) {}
}
