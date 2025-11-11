package com.careerpass.domain.introduction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;

public class IntroductionDtos {

    // 🟢 저장 요청 DTO
    @Builder
    public record CreateRequest(
            @NotNull(message = "userId는 필수입니다.")
            Long userId,

            @NotBlank(message = "지원 직무(jobApplied)는 비어 있을 수 없습니다.")
            @Size(max = 20, message = "지원 직무는 20자를 초과할 수 없습니다.")
            String jobApplied,

            @NotBlank(message = "자기소개 내용(introText)은 비어 있을 수 없습니다.")
            String introText,

            // 클라이언트에서 생략 가능하도록 null 허용
            @PastOrPresent(message = "제출 시간(submissionTime)은 과거 또는 현재여야 합니다.")
            LocalDateTime submissionTime
    ) {}

    // 🟢 응답 DTO
    @Builder
    public record Response(
            Long id,
            Long userId,
            String jobApplied,
            String introText,
            LocalDateTime submissionTime
    ) {}

    // 🟢 (선택) 간단 응답: 생성 완료 시 반환용
    @Builder
    public record CreateResponse(
            Long introductionId,
            int introLength
    ) {}
}