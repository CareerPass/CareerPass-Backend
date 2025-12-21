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
     *   2) 응답에서 id를 꺼내서 /api/users/{id}/profile 로 PATCH 요청
     */
    @Operation(
            summary = "학습프로필 수정",
            description = "nickname, major, targetJob만 수정 가능 (email은 항상 read-only)"
    )
    @PatchMapping("/{id}/profile")
    public ResponseEntity<LearningProfileResponse> updateUserProfile(
            @PathVariable @Positive(message = "id는 양수여야 합니다.") Long id,
            @RequestBody UpdateProfileRequest req
    ) {
        return ResponseEntity.ok(userService.updateProfile(id, req));
    }
}