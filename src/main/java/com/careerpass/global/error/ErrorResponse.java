package com.careerpass.global.error;

import java.time.LocalDateTime;
import java.util.List;

// API 에러 응답을 표준화하는 클래스
public class ErrorResponse {
    private final String code;
    private final String message;
    private final int status;
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final List<FieldError> errors;

    public ErrorResponse(ErrorCode ec, List<FieldError> errors) {
        this.code = ec.getCode();
        this.message = ec.getMessage();
        this.status = ec.getStatus().value();
        this.errors = errors;
    }
    public static ErrorResponse of(ErrorCode ec) { return new ErrorResponse(ec, List.of()); }
    public static ErrorResponse of(ErrorCode ec, List<FieldError> errors) { return new ErrorResponse(ec, errors); }

    public record FieldError(String field, String value, String reason) {
        public static FieldError of(String field, Object value, String reason) {
            return new FieldError(field, value == null ? "" : String.valueOf(value), reason);
        }
    }
}