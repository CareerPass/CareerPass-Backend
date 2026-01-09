package com.careerpass.domain.user.service;

import com.careerpass.domain.feedback.entity.FeedbackType;
import com.careerpass.domain.feedback.repository.FeedbackRepository;
import com.careerpass.domain.feedback.repository.InterviewSessionRepository;
import com.careerpass.domain.user.dto.LearningProfileResponse;
import com.careerpass.domain.user.dto.UpdateProfileRequest;
import com.careerpass.domain.user.entity.SocialType;
import com.careerpass.domain.user.entity.User;
import com.careerpass.domain.user.exception.UserNotFoundException;
import com.careerpass.domain.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final FeedbackRepository feedbackRepository;
    private final InterviewSessionRepository interviewSessionRepository;

    /**
     * [프로필 수정]
     * - 이메일 제외 (닉네임, 전공, 목표 직무만 수정 가능)
     * - profileCompleted 자동 업데이트
     */
    public LearningProfileResponse updateProfile(Long id, UpdateProfileRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (isNotBlank(req.nickname())) {
            user.setNickname(req.nickname());
        }
        if (isNotBlank(req.major())) {
            user.setMajor(req.major());
        }
        if (isNotBlank(req.targetJob())) {
            user.setTargetJob(req.targetJob());
        }

        /*
         * 📌 update 후 프로필 완료 여부 다시 계산하여 반영
         * - 학습프로필 관점에서는 major/targetJob이 핵심이라 이 기준으로 true 처리
         */
        boolean completed =
                user.getMajor() != null && !user.getMajor().isBlank() &&
                        user.getTargetJob() != null && !user.getTargetJob().isBlank();
        user.setProfileCompleted(completed);

        return toLearningProfileResponse(user);
    }

    public LearningProfileResponse updateProfileOwnedByEmail(Long id, String email, UpdateProfileRequest req) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        if (!requester.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        return updateProfile(id, req);
    }

    public LearningProfileResponse updateProfileOwnedByEmail(String email, UpdateProfileRequest req) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        return updateProfileOwnedByEmail(requester.getId(), email, req);
    }

    /**
     * [내 프로필 수정 (JWT 기반)]
     * - JWT principal(email)로 사용자 조회 후
     * - nickname/major/targetJob 수정
     */
    public LearningProfileResponse updateMyProfileByEmail(String email, UpdateProfileRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found by email: " + email));

        // 기존 값 유지 + 요청 값만 덮어쓰기
        String nextNickname = coalesceNonBlank(req.nickname(), user.getNickname());
        String nextMajor = coalesceNonBlank(req.major(), user.getMajor());
        String nextTargetJob = coalesceNonBlank(req.targetJob(), user.getTargetJob());

        // 엔티티 업데이트 로직 사용
        user.updateProfile(nextNickname, nextMajor, nextTargetJob);

        // 📌 학습프로필 기준으로 completed 재정의(major/targetJob)
        boolean completed =
                user.getMajor() != null && !user.getMajor().isBlank() &&
                        user.getTargetJob() != null && !user.getTargetJob().isBlank();
        user.setProfileCompleted(completed);

        userRepository.save(user);

        return toLearningProfileResponse(user);
    }

    /**
     * [학습프로필 조회]
     * - 기본정보 + 학습프로필 완료 여부
     * - 면접/자소서 학습 이력 전체 요약 리스트 포함
     */
    public LearningProfileResponse getLearningProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return toLearningProfileResponse(user);
    }

    /**
     * [메일 기준 로그인 or 자동 회원가입]
     * - JWT subject(email) 기준으로
     *   1) 이미 존재하면: 해당 유저의 학습 프로필 반환
     *   2) 없으면: 기본값으로 새 유저 생성 후 프로필 반환
     */
    public LearningProfileResponse loginOrCreateByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);

                    // 이메일 앞부분을 기본 닉네임으로 사용 (예: yunseo0154)
                    String defaultNickname = email.split("@")[0];
                    newUser.setNickname(defaultNickname);

                    // 최초 생성 시 major/targetJob은 비워둠 → profileCompleted = false
                    newUser.setMajor(null);
                    newUser.setTargetJob(null);
                    newUser.setProfileCompleted(false);

                    // 소셜 정보 기본값 (구글 로그인 기준)
                    newUser.setSocialType(SocialType.GOOGLE);
                    newUser.setSocialNumber("GOOGLE-" + UUID.randomUUID());

                    return userRepository.save(newUser);
                });

        return toLearningProfileResponse(user);
    }

    /**
     * ✅ OAuth2 로그인 성공 시점 Upsert
     * - googleSub(= oidcUser.getSubject())를 socialNumber로 저장해서 "같은 구글 계정"을 확실히 식별
     * - 없으면 생성, 있으면 email/nickname 최신값 반영(정책에 따라 조절 가능)
     */
    public User upsertGoogleUser(String email, String name, String googleSub) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (googleSub == null || googleSub.isBlank()) {
            throw new IllegalArgumentException("googleSub(subject) is required");
        }

        return userRepository.findBySocialTypeAndSocialNumber(SocialType.GOOGLE, googleSub)
                .map(user -> {
                    // email은 구글 계정 설정에 따라 바뀔 수 있으니 최신값으로 맞춤(원하면 제거 가능)
                    user.setEmail(email);

                    // nickname은 사용자가 수정할 수 있으니까,
                    // profileCompleted=false(미완)일 때만 구글 이름으로 보정하는 걸 추천
                    if (!user.isProfileCompleted() && name != null && !name.isBlank()) {
                        user.setNickname(name);
                    }
                    return saveAndResolve(user, email, googleSub);
                })
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(user -> {
                            // 기존 이메일 유저가 있으면 Google 식별자를 연결해서 중복 생성 방지
                            user.setSocialType(SocialType.GOOGLE);
                            user.setSocialNumber(googleSub);
                            if (!user.isProfileCompleted() && name != null && !name.isBlank()) {
                                user.setNickname(name);
                            }
                            return saveAndResolve(user, email, googleSub);
                        })
                        .orElseGet(() -> {
                            User newUser = User.builder()
                                    .email(email)
                                    .nickname((name == null || name.isBlank()) ? email.split("@")[0] : name)
                                    .major(null)
                                    .targetJob(null)
                                    .profileCompleted(false)
                                    .socialType(SocialType.GOOGLE)
                                    .socialNumber(googleSub) // ✅ 핵심
                                    .build();
                            return saveAndResolve(newUser, email, googleSub);
                        }));
    }

    /**
     * [엔티티 → LearningProfileResponse 변환 메서드]
     * - 기본정보 + 학습프로필 완료 여부
     * - 인터뷰/자소서 리스트를 한 번에 세팅
     */
    private LearningProfileResponse toLearningProfileResponse(User user) {

        boolean profileCompleted =
                user.getMajor() != null && !user.getMajor().isBlank() &&
                        user.getTargetJob() != null && !user.getTargetJob().isBlank();

        List<LearningProfileResponse.FeedbackSummary> introductionSummaries =
                findFeedbackSummaries(user.getId(), FeedbackType.INTRODUCTION);
        List<LearningProfileResponse.FeedbackSummary> interviewSummaries =
                findFeedbackSummaries(user.getId(), FeedbackType.INTERVIEW);

        return LearningProfileResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .major(user.getMajor())
                .targetJob(user.getTargetJob())
                .profileCompleted(profileCompleted)
                .introductionFeedbacks(introductionSummaries)
                .interviewFeedbacks(interviewSummaries)
                .build();
    }

    private List<LearningProfileResponse.FeedbackSummary> findFeedbackSummaries(Long userId, FeedbackType type) {
        if (type == FeedbackType.INTERVIEW) {
            return interviewSessionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                    .stream()
                    .map(session -> LearningProfileResponse.FeedbackSummary.builder()
                            .id(session.getId())
                            .title("면접 " + session.getId())
                            .totalScore(session.getAverageScore() == null ? null : Math.round(session.getAverageScore()))
                            .createdAt(session.getCreatedAt())
                            .build())
                    .toList();
        }
        return feedbackRepository.findByUserIdAndFeedbackTypeOrderByCreatedAtDesc(userId, type)
                .stream()
                .map(f -> LearningProfileResponse.FeedbackSummary.builder()
                        .id(f.getId())
                        .title(f.getTitle())
                        .totalScore(f.getTotalScore())
                        .createdAt(f.getCreatedAt())
                        .build())
                .toList();
    }

    private User saveAndResolve(User user, String email, String googleSub) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            return resolveAfterConstraintViolation(email, googleSub, e);
        }
    }

    private User resolveAfterConstraintViolation(
            String email,
            String googleSub,
            DataIntegrityViolationException e
    ) {
        return userRepository.findBySocialTypeAndSocialNumber(SocialType.GOOGLE, googleSub)
                .or(() -> userRepository.findByEmail(email))
                .orElseThrow(() -> e);
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String coalesceNonBlank(String value, String fallback) {
        return isNotBlank(value) ? value : fallback;
    }
}
