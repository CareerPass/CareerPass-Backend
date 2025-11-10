package com.careerpass.domain.question.dto;

/**
 * 컨트롤러(자바 → 스프링)에서 받는 요청 바디.
 * 프론트에서 넘어오는 필드명 그대로 사용(카멜케이스).
 */
public record GenerateQuestionsRequest(
        String major,     // 학과 (예: 컴퓨터공학과)
        String jobTitle   // 직무 (예: 백엔드 개발자)
) {}