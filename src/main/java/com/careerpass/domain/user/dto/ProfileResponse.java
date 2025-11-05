package com.careerpass.domain.user.dto;

public record ProfileResponse(
        Long id,
        String nickname,
        String email,
        String major,
        String targetJob
) {}