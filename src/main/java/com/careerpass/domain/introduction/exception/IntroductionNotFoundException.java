package com.careerpass.domain.introduction.exception;

public class IntroductionNotFoundException extends RuntimeException {
    // 도메인 예외 추가
    public IntroductionNotFoundException(Long id) {
        super("Introduction not found: " + id);
    }
}