package com.careerpass.domain.question.dto;

import java.util.List;

/**
 * 우리 백엔드가 프론트로 내보낼 응답(Flask 응답을 그대로 전달).
 */
public record GenerateQuestionsResponse(
        List<String> questions
) {}