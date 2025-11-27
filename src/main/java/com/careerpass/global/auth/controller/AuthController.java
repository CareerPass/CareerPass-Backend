package com.careerpass.global.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class AuthController {

    @Operation(summary = "로그아웃 api")
    @GetMapping("/logout-success")
    public Map<String, Object> logoutSuccess() {
        return Map.of(
                "message", "✅ 로그아웃이 완료되었습니다.",
                "timestamp", LocalDateTime.now().toString()
        );
    }
}