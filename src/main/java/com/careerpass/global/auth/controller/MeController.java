package com.careerpass.global.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * ✅ 실제 /me
     * - 로그인 안 되어 있으면 401
     * - 로그인 되어 있으면 OIDC 사용자 정보(email, name)를 반환
     *
     * ⚠️ major/job 같은 "학습프로필" 정보는 다음 단계에서 DB(User)와 연결해서 채울 것
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Unauthorized"
            ));
        }

        Map<String, Object> res = new HashMap<>();
        res.put("email", oidcUser.getEmail());          // ✅ 구글에서 받은 이메일
        res.put("name", oidcUser.getFullName());        // ✅ 구글 profile name (없을 수도 있음)

        // 다음 단계(DB 연동)에서 채울 값들: 지금은 null로 내려서 프론트가 분기할 수 있게만
        res.put("major", null);
        res.put("job", null);

        // 통계/목록도 DB 연결 후 제공(지금은 0)
        res.put("totalInterviewCount", 0);
        res.put("totalIntroductionCount", 0);

        return ResponseEntity.ok(res);
    }
}