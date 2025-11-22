package com.careerpass.domain.user.service;

import com.careerpass.domain.user.dto.CreateUserRequest;
import com.careerpass.domain.user.dto.UpdateProfileRequest;
import com.careerpass.domain.user.dto.LearningProfileResponse;
import com.careerpass.domain.user.entity.User;
import com.careerpass.domain.user.repository.UserRepository;
import com.careerpass.domain.user.exception.UserNotFoundException;
import com.careerpass.domain.user.exception.DuplicateEmailException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * UserService
 * - DTO(CreateUserRequest, UpdateProfileRequest, LearningProfileResponse)를 사용하도록 리팩토링
 * - 이메일은 중복 불가 & 수정 불가
 * - nickname, email, major, targetJob 4개 필드만 사용
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /**
     * [1️⃣ 사용자 생성]
     * - 이메일이 이미 존재하면 DuplicateEmailException(409)
     * - 이메일은 최초 생성 시에만 세팅 (이후 수정 불가)
     */
    public LearningProfileResponse create(CreateUserRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new DuplicateEmailException(req.email());
        }

        User user = new User();
        user.setNickname(req.nickname());
        user.setEmail(req.email());
        user.setMajor(req.major());
        user.setTargetJob(req.targetJob());

        // 소셜 정보가 아직 없으므로 로컬 기본값 채워서 NOT NULL 회피
        user.setSocialType(com.careerpass.domain.user.entity.SocialType.GOOGLE);
        user.setSocialNumber("LOCAL-" + UUID.randomUUID().toString()); // 임시 식별자

        userRepository.save(user);
        return toLearningProfileResponse(user);
    }

    /**
     * [2️⃣ 단일 조회]
     * - id 기준으로 사용자 조회
     * - 존재하지 않으면 UserNotFoundException 발생
     */
    public LearningProfileResponse getById(Long id) {
        return userRepository.findById(id)
                .map(this::toLearningProfileResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * [3️⃣ 전체 조회]
     * - 모든 사용자 리스트 조회
     * - (관리/테스트 용도, 실제 UI에서 안 쓰면 나중에 지워도 됨)
     */
    public List<LearningProfileResponse> getAll() {
        return userRepository.findAll().stream()
                .map(this::toLearningProfileResponse)
                .toList();
    }

    /**
     * [4️⃣ 프로필 수정]
     * - 이메일 제외 (닉네임, 전공, 목표 직무만 수정 가능)
     * - 존재하지 않으면 IllegalArgumentException 발생
     */
    public LearningProfileResponse updateProfile(Long id, UpdateProfileRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // null 체크 후 수정 (부분 수정 가능)
        if (req.nickname() != null) user.setNickname(req.nickname());
        if (req.major() != null) user.setMajor(req.major());
        if (req.targetJob() != null) user.setTargetJob(req.targetJob());

        return toLearningProfileResponse(user);
    }

    /**
     * [5️⃣ 학습프로필 조회]
     * - 기본정보 + 학습프로필 완료 여부
     * - 최근 면접/자소서 요약은 일단 null (나중에 연결)
     */
    public LearningProfileResponse getLearningProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return toLearningProfileResponse(user);
    }

    /**
     * [💡 엔티티 → LearningProfileResponse 변환 메서드]
     * - 응답 형식 통일
     */
    private LearningProfileResponse toLearningProfileResponse(User user) {
        boolean profileCompleted =
                user.getMajor() != null && !user.getMajor().isBlank() &&
                        user.getTargetJob() != null && !user.getTargetJob().isBlank();

        return LearningProfileResponse.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .major(user.getMajor())
                .targetJob(user.getTargetJob())
                .profileCompleted(profileCompleted)
                // TODO: 실제 최근 면접/자소서 요약 붙일 때 여기 채우기
                .recentInterview(null)
                .recentIntroduction(null)
                .build();
    }
}