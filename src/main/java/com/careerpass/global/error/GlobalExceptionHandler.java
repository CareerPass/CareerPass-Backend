package com.careerpass.global.error;

import com.careerpass.domain.introduction.exception.IntroductionNotFoundException;
import com.careerpass.global.error.ErrorResponse.FieldError;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ @RequestBody @Valid 검증 실패 (단 하나만 유지)
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
    public ResponseEntity<ErrorResponse> handleFeedbackNotFound(
            com.careerpass.domain.feedback.exception.FeedbackNotFoundException e) {
        return ResponseEntity
                .status(ErrorCode.FEEDBACK_NOT_FOUND.getStatus())
                .body(ErrorResponse.of(ErrorCode.FEEDBACK_NOT_FOUND));
    }

    // 도메인: user — 404
    @ExceptionHandler(com.careerpass.domain.user.exception.UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(RuntimeException e) {
        return ResponseEntity.status(ErrorCode.USER_NOT_FOUND.getStatus())
                .body(ErrorResponse.of(ErrorCode.USER_NOT_FOUND));
    }

    // 도메인: user — 409
    @ExceptionHandler(com.careerpass.domain.user.exception.DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(RuntimeException e) {
        return ResponseEntity.status(ErrorCode.DUPLICATE_EMAIL.getStatus())
                .body(ErrorResponse.of(ErrorCode.DUPLICATE_EMAIL));
    }

    // 도메인: interview — 400 (그대로 유지)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // ❌ (중복 제거) @ExceptionHandler(MethodArgumentNotValidException.class)
    // public ResponseEntity<String> handleValidation(MethodArgumentNotValidException e) { ... }

    // 415
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<String> handleUnsupported(HttpMediaTypeNotSupportedException e) {
        return ResponseEntity.status(415).body("지원하지 않는 Content-Type 입니다.");
    }

    // WebClient 4xx/5xx 응답
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientResponse(WebClientResponseException e) {
        return ResponseEntity
                .status(e.getStatusCode())
                .body(new ErrorResponse(
                        "PYTHON_API_RESPONSE_ERROR",
                        e.getStatusCode().value(),
                        e.getResponseBodyAsString(),
                        null
                ));
    }

    // WebClient 요청/연결 실패
    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ErrorResponse> handleWebClientRequest(WebClientRequestException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse(
                        "PYTHON_API_REQUEST_ERROR",
                        HttpStatus.BAD_GATEWAY.value(),
                        e.getMessage(),
                        null
                ));
    }

    // 타임아웃 (WebClient 또는 서블릿 비동기)
    @ExceptionHandler({TimeoutException.class, AsyncRequestTimeoutException.class})
    public ResponseEntity<ErrorResponse> handleTimeouts(Exception e) {
        return ResponseEntity
                .status(HttpStatus.GATEWAY_TIMEOUT)
                .body(new ErrorResponse(
                        "PYTHON_API_TIMEOUT",
                        HttpStatus.GATEWAY_TIMEOUT.value(),
                        "AI 서버 응답이 시간 제한을 초과했습니다.",
                        null
                ));
    }

    // 서비스 레이어에서 전달되는 ResponseStatusException 유지
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException e) {
        return ResponseEntity
                .status(e.getStatusCode())
                .body(new ErrorResponse(
                        e.getStatusCode().toString(),
                        e.getStatusCode().value(),
                        e.getReason(),
                        null
                ));
    }

    // ❌ (중복 제거) @ExceptionHandler(Exception.class)
    // public ResponseEntity<String> handleUnknown(Exception e) { ... }

    // ✅ 그 외 전부 — ErrorResponse로 일관
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleEtc(Exception e) {
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    // 도메인: roadmap
    // 도메인: roadmap — 404 처리
    @ExceptionHandler(com.careerpass.domain.roadmap.exception.RoadmapNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoadmapNotFound(
            com.careerpass.domain.roadmap.exception.RoadmapNotFoundException e
    ) {
        return ResponseEntity
                .status(ErrorCode.ROADMAP_NOT_FOUND.getStatus())
                .body(ErrorResponse.of(ErrorCode.ROADMAP_NOT_FOUND, e.getMessage()));
    }
}
