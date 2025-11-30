package com.careerpass.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 구글 OAuth 이후, 프론트에서 넘겨주는 로그인 요청 DTO
 * - 이메일만 사용
 */
public record LoginRequest(
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email
) {}