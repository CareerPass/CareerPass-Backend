package com.careerpass.domain.introduction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class IntroductionDtos {

    // 저장 요청
    public record CreateRequest(
            @NotNull(message = "userId는 필수입니다.")
            Long userId,

            @NotBlank(message = "지원 직무(jobApplied)는 비어 있을 수 없습니다.")
            @Size(max = 20, message = "지원 직무는 20자를 초과할 수 없습니다.")
            String jobApplied,

            @NotBlank(message = "자기소개 내용(introText)은 비어 있을 수 없습니다.")
            String introText,

            // 클라이언트에서 시간 안 줄 수도 있으니 옵션(널이면 서버에서 now()로 대체)
            @PastOrPresent(message = "제출 시간(submissionTime)은 과거 또는 현재여야 합니다.")
            LocalDateTime submissionTime
    ) {}

    // 단건 조회/리스트 응답
    public record Response(
            Long id,
            Long userId,
            String jobApplied,
            String introText,
            LocalDateTime submissionTime
    ) {}
}