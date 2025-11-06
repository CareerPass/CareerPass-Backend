package com.careerpass.global.error;

import org.springframework.http.HttpStatus;

// 공통 및 도메인별 에러 코드를 정의하는 열거형
public enum ErrorCode {
    // common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "요청 값이 올바르지 않습니다."),
    MESSAGE_NOT_READABLE(HttpStatus.BAD_REQUEST, "C003", "요청 본문을 읽을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C004", "허용되지 않은 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C999", "서버 오류가 발생했습니다."),
    // introduction
    INTRODUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "I001", "자기소개서를 찾을 수 없습니다."),
    // feedback
    FEEDBACK_NOT_FOUND(HttpStatus.NOT_FOUND, "F001", "해당 피드백을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status; this.code = code; this.message = message;
    }
    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}