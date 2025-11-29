package com.careerpass.domain.user.controller;

import com.careerpass.domain.user.dto.CreateUserRequest;
import com.careerpass.domain.user.dto.UpdateProfileRequest;
import com.careerpass.domain.user.dto.LearningProfileResponse;
import com.careerpass.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * UserController (LearningProfileResponse 기반)
 */
@Validated
@Tag(name = "User API", description = "User API for managing learning profile (nickname, major, target job)")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * [1️⃣ 사용자 생성]
     * - 이메일은 OAuth2 로그인 정보(OAuth2User)에서 직접 추출
     * - CreateUserRequest 에는 이메일 없음
     */
    @Operation(summary = "사용자 생성 api")
    @PostMapping
    public ResponseEntity<LearningProfileResponse> createUser(
            @AuthenticationPrincipal OAuth2User oauth2User,
            @RequestBody @Valid CreateUserRequest req
    ) {
        // 🔹 OAuth2User에서 email attribute 추출 (구글 로그인 기준)
        String email = oauth2User != null ? oauth2User.getAttribute("email") : null;

        if (email == null || email.isBlank()) {
            throw new IllegalStateException("소셜 로그인 정보에 이메일이 없습니다. OAuth2 로그인 설정을 확인해주세요.");
        }

        LearningProfileResponse created = userService.create(email, req);

        return ResponseEntity
                .created(URI.create("/api/users/" + created.getEmail()))
                .body(created);
    }

    /**
     * [2️⃣ 전체 사용자 조회]
     */
    @Operation(summary = "전체 사용자 조회 api")
    @GetMapping
    public List<LearningProfileResponse> getAllUsers() {
        return userService.getAll();
    }

    /**
     * [3️⃣ 단일 사용자 조회]
     */
    @Operation(summary = "단일 사용자 조회 api")
    @GetMapping("/{id}")
    public ResponseEntity<LearningProfileResponse> getUserById(
            @PathVariable @Positive(message = "id는 양수여야 합니다.") Long id
    ) {
        return ResponseEntity.ok(userService.getLearningProfile(id));
    }

    /**
     * [4️⃣ 프로필 수정]
     * nickname, major, targetJob만 수정
     */
    @Operation(summary = "프로필 수정 api", description = "nickname, major, targetJob만 수정 가능")
    @PatchMapping("/{id}/profile")
    public ResponseEntity<LearningProfileResponse> updateUserProfile(
            @PathVariable Long id,
            @RequestBody UpdateProfileRequest req
    ) {
        return ResponseEntity.ok(userService.updateProfile(id, req));
    }
}