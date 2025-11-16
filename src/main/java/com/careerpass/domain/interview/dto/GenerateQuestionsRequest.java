package com.careerpass.domain.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 면접 질문 생성 요청 DTO
 * - 프론트에서는 userId + (선택) 자소서만 넘기고
 * - 백엔드에서 userId로 학습 프로필을 조회해 major/jobTitle을 채운다.
 */
@Schema(description = "면접 질문 생성 요청 DTO")
public record GenerateQuestionsRequest(

        @NotNull
        @Schema(description = "유저 ID", example = "10")
        Long userId,

        @Schema(
                description = "지원자의 자기소개서 본문(선택). 비어 있으면 학습 프로필 정보만으로 질문 생성",
                example = "저는 백엔드 개발자로 성장하기 위해..."
        )
        String coverLetter
) {}