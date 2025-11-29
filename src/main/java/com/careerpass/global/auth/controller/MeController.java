package com.careerpass.global.auth.controller;

import com.careerpass.domain.user.dto.LearningProfileResponse;
import com.careerpass.domain.user.entity.User;
import com.careerpass.domain.user.repository.UserRepository;
import com.careerpass.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeController {

    private final UserRepository userRepository;
    private final UserService userService;

    // 헬스 체크 (프론트에서 /api/health 호출 중이면 이걸로 맞춰줌)
    @GetMapping("/health")
    public String health() {
        return "UP";
    }

    /**
     * 현재 로그인한 사용자의 학습 프로필 조회
     * - OIDC에서 email 꺼냄
     * - email로 DB User 조회
     * - 없으면 404 USER_NOT_FOUND → 프론트가 이걸 보고 createUser() 호출
     * - 있으면 LearningProfileResponse 반환 (profileCompleted 포함)
     */
    @GetMapping("/me")
    public LearningProfileResponse me(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null || oidcUser.getEmail() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
        }

        String email = oidcUser.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        // UserService에서 우리가 이미 만든 로직 재사용
        return userService.getLearningProfile(user.getId());
    }
}