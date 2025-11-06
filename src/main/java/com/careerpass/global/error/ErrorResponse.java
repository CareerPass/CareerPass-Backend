package com.careerpass.global.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
// API 에러 응답을 표준화하는 클래스
public class ErrorResponse {
    private final String code;
    private final String message;
    private final int status;
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final List<FieldError> errors;

    // ErrorCode + errors
    public ErrorResponse(ErrorCode ec, List<FieldError> errors) {
        this.code = ec.getCode();
        this.message = ec.getMessage();
        this.status = ec.getStatus().value();
        this.errors = errors;
    }
    // 코드/상태/메시지 직접 지정용
    public ErrorResponse(String code, int status, String message, List<FieldError> errors) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.errors = errors;
    }
    // ✅ 정적 팩토리
    public static ErrorResponse of(ErrorCode ec) { return new ErrorResponse(ec, List.of()); }
    public static ErrorResponse of(ErrorCode ec, List<FieldError> errors) { return new ErrorResponse(ec, errors); }
    // ✅ 메시지 전용 팩토리
    public static ErrorResponse of(ErrorCode code, String message) {
        return new ErrorResponse(
                code.getCode(),
                code.getStatus().value(),
                message,
                null
        );
    }
    // ✅ Jackson 직렬화용 getter 추가 (없으면 JSON 변환 불가)
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public int getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public List<FieldError> getErrors() { return errors; }

    // 필드 에러 정의
    public record FieldError(String field, String value, String reason) {
        public static FieldError of(String field, Object value, String reason) {
            return new FieldError(field, value == null ? "" : String.valueOf(value), reason);
        }
    }
}