package com.careerpass.global.auth.controller;

import com.careerpass.domain.user.dto.LearningProfileResponse;
import com.careerpass.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class MeController {

    private final UserService userService;

    @GetMapping("/health")
    public String health() {
        return "UP";
    }

    /**
     * 로그인한 사용자의 학습 프로필 조회
     * - 구글 OIDC에서 email 꺼내서 tb_user와 매핑
     * - 없으면 404(UserNotFoundException) or 404 직접 던져도 됨
     */
    @GetMapping("/me")
    public LearningProfileResponse me(@AuthenticationPrincipal OidcUser user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        String email = user.getEmail();
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이메일 정보를 찾을 수 없습니다.");
        }

        // 로그인 또는 자동 가입 처리
        return userService.loginOrCreateByEmail(email);
    }
}