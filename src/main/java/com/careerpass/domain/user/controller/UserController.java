package com.careerpass.domain.user.controller;

import com.careerpass.domain.user.dto.CreateUserRequest;
import com.careerpass.domain.user.dto.LearningProfileResponse;
import com.careerpass.domain.user.dto.UpdateProfileRequest;
import com.careerpass.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * UserController
 * - 전역 인증(/me, /logout-success)은 global.auth.controller에 위임
 * - 여기서는 순수하게 User/학습프로필 도메인 API만 제공
 */
@Validated
@Tag(
        name = "User API",
        description = "유저 학습프로필 관리 API (관리자/테스트/도메인용)"
)
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * [1️⃣ 사용자 생성 (테스트/관리용)]
     * - 실제 운영 흐름에서는 구글 OAuth + MeController에서 loginOrCreateByEmail()로 처리
     * - 필요하다면 Swagger 등에서 수동으로 유저를 생성할 때 사용
     */
    @Operation(summary = "사용자 수동 생성 (테스트용)")
    @PostMapping
    public ResponseEntity<LearningProfileResponse> createUser(
            @RequestBody @Valid CreateUserRequest req
    ) {
        LearningProfileResponse created = userService.create(req);

        // email 기반으로 Location 헤더 설정 (id가 DTO에 없으므로 email을 사용)
        URI location = URI.create("/api/users?email=" + created.getEmail());

        return ResponseEntity
                .created(location)
                .body(created);
    }

    /**
     * [2️⃣ 전체 사용자 조회]
     * - 관리자/디버깅용
     */
    @Operation(summary = "전체 사용자 조회")
    @GetMapping
    public ResponseEntity<List<LearningProfileResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAll());
    }

    /**
     * [3️⃣ ID 기준 단일 사용자 조회]
     * - 학습프로필까지 포함된 응답 반환
     */
    @Operation(summary = "ID로 사용자 조회")
    @GetMapping("/{id}")
    public ResponseEntity<LearningProfileResponse> getUserById(
            @PathVariable @Positive(message = "id는 양수여야 합니다.") Long id
    ) {
        return ResponseEntity.ok(userService.getLearningProfile(id));
    }

    /**
     * [4️⃣ 프로필 수정]
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