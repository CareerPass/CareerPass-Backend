package com.careerpass.domain.introduction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class IntroductionDtos {

    // 저장 요청
    public record CreateRequest(
            @NotNull Long userId,
            @NotBlank @Size(max = 20) String jobApplied,
            @NotBlank String introText,
            // 클라이언트에서 시간 안 줄 수도 있으니 옵션(널이면 서버에서 now()로 대체)
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