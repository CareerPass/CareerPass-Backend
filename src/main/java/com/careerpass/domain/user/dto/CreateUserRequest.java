package com.careerpass.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,

        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @NotBlank(message = "전공은 필수입니다.")
        String major,

        @NotBlank(message = "목표 직무는 필수입니다.")
        String targetJob
) {}