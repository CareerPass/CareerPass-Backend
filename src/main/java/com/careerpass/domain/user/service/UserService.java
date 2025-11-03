package com.careerpass.domain.user.service;

import com.careerpass.domain.user.dto.CreateUserRequest;
import com.careerpass.domain.user.dto.UpdateProfileRequest;
import com.careerpass.domain.user.dto.ProfileResponse;
import com.careerpass.domain.user.entity.User;
import com.careerpass.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UserService
 * - DTO(CreateUserRequest, UpdateProfileRequest, ProfileResponse)를 사용하도록 리팩토링됨
 * - 이메일은 중복 불가 & 수정 불가
 * - name, email, major, targetJob 4개 필드만 사용
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /**
     * [1️⃣ 사용자 생성]
     * - 이메일이 이미 존재하면 DuplicateKeyException(409)
     * - 이메일은 최초 생성 시에만 세팅 (이후 수정 불가)
     */
    public ProfileResponse create(CreateUserRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new DuplicateKeyException("Email already exists");
        }

        User user = new User();
        user.setNickname(req.nickname());
        user.setEmail(req.email());
        user.setMajor(req.major());
        user.setTargetJob(req.targetJob());

        userRepository.save(user);
        return toDto(user);
    }

    /**
     * [2️⃣ 단일 조회]
     * - id 기준으로 사용자 조회
     * - 존재하지 않으면 IllegalArgumentException 발생
     */
    public ProfileResponse getById(Long id) {
        return userRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    /**
     * [3️⃣ 전체 조회]
     * - 모든 사용자 리스트 조회
     */
    public List<ProfileResponse> getAll() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * [4️⃣ 프로필 수정]
     * - 이메일 제외 (이름, 전공, 목표 직무만 수정 가능)
     * - 존재하지 않으면 IllegalArgumentException 발생
     */
    public ProfileResponse updateProfile(Long id, UpdateProfileRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // null 체크 후 수정 (부분 수정 가능)
        if (req.nickname() != null) user.setNickname(req.nickname());
        if (req.major() != null) user.setMajor(req.major());
        if (req.targetJob() != null) user.setTargetJob(req.targetJob());

        return toDto(user);
    }

    /**
     * [💡 엔티티 → DTO 변환 메서드]
     * - 응답 형식을 통일하기 위해 사용
     */
    private ProfileResponse toDto(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getMajor(),
                user.getTargetJob()
        );
    }
}