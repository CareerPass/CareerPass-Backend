package com.careerpass.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// 회원 생성 요청 DTO
public record CreateUserRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,

        @NotBlank(message = "전공은 필수입니다.")
        String major,

        @NotBlank(message = "목표 직무는 필수입니다.")
        String targetJob
) {}