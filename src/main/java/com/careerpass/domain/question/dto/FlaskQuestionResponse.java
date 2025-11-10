package com.careerpass.domain.question.dto;

import java.util.List;

/**
 * Flask가 주는 응답 스키마를 그대로 매핑.
 */
public record FlaskQuestionResponse(
        List<String> questions
) {}