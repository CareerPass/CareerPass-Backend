package com.careerpass.domain.user.exception;

public class UserNotFoundException extends RuntimeException {

    // 기존: id 기준 생성자
    public UserNotFoundException(Long id) {
        super("User not found: id=" + id);
    }

    // ✅ 추가: email 기준 생성자
    public UserNotFoundException(String email) {
        super("User not found: email=" + email);
    }
}