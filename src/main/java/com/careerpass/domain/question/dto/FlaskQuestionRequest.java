package com.careerpass.domain.question.dto;

/**
 * Flask(파이썬)으로 전달할 요청 바디.
 * 파이썬 쪽 요구에 맞춰 snake_case 사용.
 */
public record FlaskQuestionRequest(
        String major,
        String job_title
) {}