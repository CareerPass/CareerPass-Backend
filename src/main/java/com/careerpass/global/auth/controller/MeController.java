package com.careerpass.global.auth.controller;

import com.careerpass.domain.user.dto.LearningProfileResponse;
import com.careerpass.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class MeController {

    private final UserService userService;

    /**
     * 헬스 체크 (nginx / 배포 확인용)
     */
    @GetMapping("/health")
    public String health() {
        return "UP";
    }

    /**
     * ✅ 현재 로그인한 사용자 정보 조회
     *
     * 흐름:
     * 1️⃣ JwtAuthenticationFilter → Authentication.principal = email
     * 2️⃣ email 기준으로 DB 조회
     * 3️⃣ 없으면 자동 생성(loginOrCreate)
     * 4️⃣ LearningProfileResponse 반환
     */
    @GetMapping("/me")
    public LearningProfileResponse me(Authentication authentication) {

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        // JWT subject = email
        String email = authentication.getPrincipal().toString();

        // 🔥 핵심: DB 기준으로 로그인 or 생성
        return userService.loginOrCreateByEmail(email);
    }
}