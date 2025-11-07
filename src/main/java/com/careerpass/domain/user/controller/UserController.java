package com.careerpass.domain.user.controller;

import com.careerpass.domain.user.dto.CreateUserRequest;
import com.careerpass.domain.user.dto.UpdateProfileRequest;
import com.careerpass.domain.user.dto.ProfileResponse;
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
 * UserController (DTO 기반 버전)
 * - User 엔티티 직접 노출하지 않음
 * - Swagger용 어노테이션 유지
 * - 학습 프로필(이름, 이메일, 전공, 목표직무) 관련 CRUD 담당
 */
@Validated
@Tag(name = "User API", description = "User API for managing user profile (name, email, major, target job)")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * [1️⃣ 사용자 생성]
     * - 이메일 중복 시 DuplicateKeyException (409)
     */
    @Operation(summary = "Create a new user (email must be unique)")
    @PostMapping
    public ResponseEntity<ProfileResponse> createUser(@RequestBody @Valid CreateUserRequest req) {
        ProfileResponse created = userService.create(req);
        return ResponseEntity
                .created(URI.create("/api/users/" + created.id()))
                .body(created);
    }

    /**
     * [2️⃣ 전체 사용자 조회]
     */
    @Operation(summary = "Get all users")
    @GetMapping
    public List<ProfileResponse> getAllUsers() {
        return userService.getAll();
    }

    /**
     * [3️⃣ 단일 사용자 조회]
     */
    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getUserById(@PathVariable @Positive(message="id는 양수여야 합니다.") Long id) {
        try {
            return ResponseEntity.ok(userService.getById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * [4️⃣ 학습 프로필 수정]
     * - 이메일 수정 불가 (이름, 전공, 목표직무만)
     */
    @Operation(summary = "Update user profile (name, major, target job only)")
    @PatchMapping("/{id}/profile")
    public ResponseEntity<ProfileResponse> updateUserProfile(
            @PathVariable Long id,
            @RequestBody UpdateProfileRequest req) {

        try {
            return ResponseEntity.ok(userService.updateProfile(id, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}