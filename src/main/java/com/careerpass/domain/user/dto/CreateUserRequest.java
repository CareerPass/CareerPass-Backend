package com.careerpass.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String nickname,
        @Email @NotBlank String email,
        String major,
        String targetJob
) {}