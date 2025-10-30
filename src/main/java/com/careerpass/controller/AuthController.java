package com.careerpass.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class AuthController {

    @GetMapping("/logout-success")
    public Map<String, Object> logoutSuccess() {
        return Map.of(
                "message", "✅ 로그아웃이 완료되었습니다.",
                "timestamp", LocalDateTime.now().toString()
        );
    }
}