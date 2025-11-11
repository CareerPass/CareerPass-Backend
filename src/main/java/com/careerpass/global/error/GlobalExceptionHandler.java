package com.careerpass.global.error;

import com.careerpass.domain.introduction.exception.IntroductionNotFoundException;
import com.careerpass.global.error.ErrorResponse.FieldError;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

// 전역 예외 처리기
@RestControllerAdvice
public class GlobalExceptionHandler {

    // @Valid 본문 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBody(MethodArgumentNotValidException e) {
        var errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> FieldError.of(fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage()))
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, errors));
    }

    // @RequestParam/@PathVariable 검증 실패
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleParam(ConstraintViolationException e) {
        var errors = e.getConstraintViolations().stream()
                .map(v -> FieldError.of(v.getPropertyPath().toString(), v.getInvalidValue(), v.getMessage()))
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, errors));
    }

    // 요청 파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissing(MissingServletRequestParameterException e) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE));
    }

    // JSON 파싱 실패 / Content-Type 문제
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(ErrorCode.MESSAGE_NOT_READABLE));
    }

    // 허용되지 않은 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getStatus())
                .body(ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED));
    }

    // 도메인: introduction
    @ExceptionHandler(IntroductionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleIntroNotFound(IntroductionNotFoundException e) {
        return ResponseEntity
                .status(ErrorCode.INTRODUCTION_NOT_FOUND.getStatus())
                .body(ErrorResponse.of(ErrorCode.INTRODUCTION_NOT_FOUND, e.getMessage()));
    }

    // 도메인: feedback
    @ExceptionHandler(com.careerpass.domain.feedback.exception.FeedbackNotFoundException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> handleFeedbackNotFound(
            com.careerpass.domain.feedback.exception.FeedbackNotFoundException e) {
        return org.springframework.http.ResponseEntity
                .status(ErrorCode.FEEDBACK_NOT_FOUND.getStatus())
                .body(ErrorResponse.of(ErrorCode.FEEDBACK_NOT_FOUND));
    }

    // 도메인: user
    // ✅ [user] 존재하지 않을 때 404
    @ExceptionHandler(com.careerpass.domain.user.exception.UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(RuntimeException e) {
        return ResponseEntity.status(ErrorCode.USER_NOT_FOUND.getStatus())
                .body(ErrorResponse.of(ErrorCode.USER_NOT_FOUND));
    }

    // ✅ [user] 이메일 중복 409
    @ExceptionHandler(com.careerpass.domain.user.exception.DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(RuntimeException e) {
        return ResponseEntity.status(ErrorCode.DUPLICATE_EMAIL.getStatus())
                .body(ErrorResponse.of(ErrorCode.DUPLICATE_EMAIL));
    }

    // 도메인: interview
    // 400: 잘못된 요청 (파라미터 검증 실패, 빈 파일 등)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // 400: @Validated 바디/파라미터 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body("요청 값이 올바르지 않습니다.");
    }

    // 415: Content-Type 틀림 (multipart/form-data 아님)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<String> handleUnsupported(HttpMediaTypeNotSupportedException e) {
        return ResponseEntity.status(415).body("지원하지 않는 Content-Type 입니다.");
    }

    // 500: 그 외 전부
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnknown(Exception e) {
        return ResponseEntity.status(500).body("서버 오류");
    }

    // 최종 안전망
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleEtc(Exception e) {
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}