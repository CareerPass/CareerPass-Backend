package com.careerpass.domain.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * 🎯 면접 질문 생성 요청 DTO
 * - 사용자가 선택한 전공(major)과 지원 직무(jobTitle)를 기반으로
 *   AI가 맞춤형 질문 리스트를 생성하도록 요청한다.
 */
@Builder
public record GenerateQuestionsRequest(

        /** 전공 (예: 컴퓨터공학과, 경영학과 등) */
        @NotBlank String major,

        /** 지원 직무 (예: 백엔드 개발자, 데이터 분석가 등) */
        @NotBlank String jobTitle,

        /** 생성할 질문 개수 (기본값 5, null 가능) */
        Integer count
) {}