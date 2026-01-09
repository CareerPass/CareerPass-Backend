package com.careerpass.domain.user.controller;

import com.careerpass.domain.user.dto.LearningProfileResponse;
import com.careerpass.domain.user.dto.UpdateProfileRequest;
import com.careerpass.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * UserController
 * - 전역 인증(/me, /logout-success)은 global.auth.controller에 위임
 * - 여기서는 순수하게 User/학습프로필 도메인 API만 제공
 *
 * ✅ 최소화 정책:
 * - POST /api/users (수동 생성) 제거  → OAuth 성공 시 upsert/loginOrCreate로 자동 생성
 * - GET /api/users (전체 조회) 제거   → 운영/보안 관점에서 불필요, 필요하면 나중에 관리자용으로 분리
 */
@Validated
@Tag(
        name = "User API",
        description = "유저 학습프로필 관리 API (도메인용)"
)
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * [ID 기준 단일 사용자 조회]
     * - 학습프로필까지 포함된 응답 반환
     * - 프론트는 보통 /me로 내 정보(id 포함)를 받고, 그 id로 상세 프로필을 가져올 때 사용 가능
     */
    @Operation(summary = "ID로 사용자 조회 (학습프로필 포함)")
    @GetMapping("/{id}")
    public ResponseEntity<LearningProfileResponse> getUserById(
            @PathVariable @Positive(message = "id는 양수여야 합니다.") Long id
    ) {
        return ResponseEntity.ok(userService.getLearningProfile(id));
    }

    /**
     * [프로필 수정]
     * - nickname, major, targetJob만 수정 가능 (email 수정 불가)
     * - 프론트는 일반적으로:
     *   1) 먼저 /me로 내 프로필 조회 (MeController)
     *   2) /api/users/me/profile 로 PATCH 요청 (권장)
     */
    @Operation(
            summary = "[Deprecated] 학습프로필 수정 (id 필요)",
            description = "레거시/내부용. 프론트는 /api/users/me/profile 사용",
            deprecated = true
    )
    @Deprecated
    @PatchMapping("/{id}/profile")
    public ResponseEntity<LearningProfileResponse> updateUserProfile(
            @PathVariable @Positive(message = "id는 양수여야 합니다.") Long id,
            @RequestBody @Valid UpdateProfileRequest req,
            Principal principal
    ) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return ResponseEntity.ok(userService.updateProfileOwnedByEmail(id, principal.getName(), req));
    }

    @Operation(
            summary = "내 학습프로필 수정",
            description = "프론트 기본 경로. nickname, major, targetJob만 수정 가능 (email은 항상 read-only)"
    )
    @PatchMapping("/me/profile")
    public ResponseEntity<LearningProfileResponse> updateMyProfile(
            @RequestBody @Valid UpdateProfileRequest req,
            Principal principal
    ) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return ResponseEntity.ok(userService.updateProfileOwnedByEmail(principal.getName(), req));
    }
}
