package com.careerpass.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

// 이메일은 수정 불가이므로 포함하지 않음
public record UpdateProfileRequest(
        String nickname,
        @NotBlank String major,
        @NotBlank String targetJob
) {}
