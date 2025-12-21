package com.careerpass.domain.user.controller;

import com.careerpass.domain.user.dto.LearningProfileResponse;
import com.careerpass.domain.user.dto.UpdateProfileRequest;
import com.careerpass.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserMeController {

    private final UserService userService;

    /**
     * ✅ 내 프로필 수정
     * - JWT 인증 필터에서 principal = email(String)로 세팅돼 있다는 전제
     * - email은 변경 불가
     * - nickname/major/targetJob만 수정
     */
    @PutMapping("/me")
    public ResponseEntity<LearningProfileResponse> updateMe(
            Authentication authentication,
            @RequestBody UpdateProfileRequest req
    ) {
        String email = String.valueOf(authentication.getPrincipal());
        LearningProfileResponse updated = userService.updateMyProfileByEmail(email, req);
        return ResponseEntity.ok(updated);
    }
}