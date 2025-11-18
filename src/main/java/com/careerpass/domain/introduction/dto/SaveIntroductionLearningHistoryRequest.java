package com.careerpass.domain.introduction.dto;

import lombok.Builder;

/**
 * 📨 자소서 학습 기록 저장 요청 DTO
 * - 어떤 유저가
 * - 어떤 자소서(introductionId)를 기반으로
 * - 몇 개의 질문을 학습했는지 전달
 */
@Builder
public record SaveIntroductionLearningHistoryRequest(
        Long userId,
        Long introductionId,
        int questionCount
) {}