package com.careerpass.global.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MeController {

    @GetMapping("/health")
    public String health() {
        return "UP";
    }

    /**
     * ✅ JWT 기반 /me
     * - JwtAuthenticationFilter가 SecurityContext에 Authentication을 세팅해주고
     * - principal에는 "email(String)"이 들어있다는 전제
     *
     * ⚠️ 여기서는 OidcUser(구글 유저 객체) 못 씀.
     *    OidcUser는 OAuth2 로그인 성공 핸들러에서만 직접 접근 가능하고,
     *    JWT 방식으로 전환하면 /me에서는 토큰/DB 기반으로 정보를 내려줘야 함.
     */
    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        // ✅ 인증 안 됐으면 401
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        // ✅ principal = email(String)
        String email = String.valueOf(authentication.getPrincipal());

        Map<String, Object> res = new HashMap<>();
        res.put("email", email);

        // ✅ DB 연동 전이라 name/major/job은 아직 못 채움 (다음 단계에서 User 테이블 조회로 채울 예정)
        res.put("name", null);
        res.put("major", null);
        res.put("job", null);

        // ✅ 통계도 DB 연동 후 채울 예정
        res.put("totalInterviewCount", 0);
        res.put("totalIntroductionCount", 0);

        return res;
    }
}