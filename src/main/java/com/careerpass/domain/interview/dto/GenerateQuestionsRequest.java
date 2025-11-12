package com.careerpass.domain.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 면접 질문 생성 요청 DTO
 * FastAPI(Flask) 서버로 major, jobTitle, count를 전달한다.
 */
@Schema(description = "면접 질문 생성 요청 DTO")
public record GenerateQuestionsRequest(

        @NotBlank
        @Schema(description = "지원 전공 또는 학과명", example = "컴퓨터공학과")
        String major,

        @NotBlank
        @Schema(description = "지원 직무명", example = "백엔드 개발자")
        String jobTitle,

        @Schema(description = "질문 개수 (null이면 기본 5)", example = "5", nullable = true)
        Integer count
) {}