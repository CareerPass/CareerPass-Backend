package com.careerpass.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MeController {

    @GetMapping("/health")
    public String health() {
        return "UP";
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal OidcUser user) {
        return Map.of(
                "sub", user.getSubject(), // 고유 ID
                "email", user.getEmail(), // 계정 이메일
                "name", user.getFullName(), // 프로필 이름
                "picture", user.getPicture(), // 프로필 사진 URL
                "claims", user.getClaims() // 구글이 반환한 전체 사용자 정보(토큰 정보 포함)
        );
    }
}