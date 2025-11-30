package com.careerpass.domain.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 자기소개서(이력서) AI 피드백용 DTO
 */
public class IntroductionAiDtos {

    /**
     * 스프링 → 파이썬 요청 DTO
     * (JSON 키를 파이썬이 기대하는 이름과 똑같이 맞춤)
     */
    public record IntroFeedbackRequest(
            @NotNull(message = "userId는 필수입니다.")
            Long userId,

            @NotBlank(message = "자기소개서(이력서) 내용은 필수입니다.")
            String resumeContent   // 🔴 파이썬의 ResumeInput.resumeContent 와 동일
    ) {}

    /**
     * 파이썬 → 스프링 응답 DTO
     * (FeedbackResponse와 동일 구조)
     */
    public record IntroFeedbackResponse(
            String feedback,
            Long userId
    ) {}
}