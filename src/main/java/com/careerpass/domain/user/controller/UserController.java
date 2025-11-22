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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * UserController (LearningProfileResponse 기반)
 */
@Validated
@Tag(name = "User API", description = "User API for managing learning profile (nickname, email, major, target job)")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * [1️⃣ 사용자 생성]
     */
    @Operation(summary = "Create a new user (email must be unique)")
    @PostMapping
    public ResponseEntity<LearningProfileResponse> createUser(@RequestBody @Valid CreateUserRequest req) {
        LearningProfileResponse created = userService.create(req);
        return ResponseEntity
                .created(URI.create("/api/users/" + created.getEmail()))
                .body(created);
    }

    /**
     * [2️⃣ 전체 사용자 조회]
     */
    @Operation(summary = "Get all users")
    @GetMapping
    public List<LearningProfileResponse> getAllUsers() {
        return userService.getAll();
    }

    /**
     * [3️⃣ 단일 사용자 조회]
     */
    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<LearningProfileResponse> getUserById(
            @PathVariable @Positive(message = "id는 양수여야 합니다.") Long id) {

        return ResponseEntity.ok(userService.getLearningProfile(id));
    }

    /**
     * [4️⃣ 프로필 수정]
     * nickname, major, targetJob만 수정
     */
    @Operation(summary = "Update user profile (nickname, major, target job only)")
    @PatchMapping("/{id}/profile")
    public ResponseEntity<LearningProfileResponse> updateUserProfile(
            @PathVariable Long id,
            @RequestBody UpdateProfileRequest req) {

        return ResponseEntity.ok(userService.updateProfile(id, req));
    }
}