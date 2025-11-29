package com.careerpass.domain.user.service;

import com.careerpass.domain.introduction.entity.IntroductionLearningHistory;
import com.careerpass.domain.introduction.repository.IntroductionLearningHistoryRepository;
import com.careerpass.domain.interview.entity.InterviewLearningRecord;
import com.careerpass.domain.interview.repository.InterviewLearningRecordRepository;
import com.careerpass.domain.user.dto.CreateUserRequest;
import com.careerpass.domain.user.dto.LearningProfileResponse;
import com.careerpass.domain.user.dto.UpdateProfileRequest;
import com.careerpass.domain.user.entity.SocialType;
import com.careerpass.domain.user.entity.User;
import com.careerpass.domain.user.exception.DuplicateEmailException;
import com.careerpass.domain.user.exception.UserNotFoundException;
import com.careerpass.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final InterviewLearningRecordRepository interviewLearningRecordRepository;
    private final IntroductionLearningHistoryRepository introductionLearningHistoryRepository;

    // 날짜 표시 형식: "2024.12.18"
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd");

    /**
     * [1️⃣ 사용자 생성]
     * - 이메일은 OAuth 로그인 성공 시 백엔드에서 전달받음 (CreateUserRequest에는 없음)
     * - 이메일이 이미 존재하면 DuplicateEmailException(409)
     */
    public LearningProfileResponse create(String email, CreateUserRequest req) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        User user = new User();
        user.setNickname(req.nickname());       // 입력 받는 항목
        user.setEmail(email);                   // OAuth 제공 이메일
        user.setMajor(req.major());             // 입력 받는 항목
        user.setTargetJob(req.targetJob());     // 입력 받는 항목

        // 소셜 정보 기본값 (NOT NULL 피하기용)
        user.setSocialType(SocialType.GOOGLE);
        user.setSocialNumber("GOOGLE-" + UUID.randomUUID());

        /**
         * 📌 최초 생성 시 프로필 완료 여부 반영
         * major & targetJob 이 세팅되면 true
         */
        boolean completed =
                req.major() != null && !req.major().isBlank() &&
                        req.targetJob() != null && !req.targetJob().isBlank();
        user.setProfileCompleted(completed);

        userRepository.save(user);
        return toLearningProfileResponse(user);
    }

    /**
     * [2️⃣ 단일 조회]
     * - id 기준으로 사용자 조회
     * - 존재하지 않으면 UserNotFoundException 발생
     */
    public LearningProfileResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return toLearningProfileResponse(user);
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
     * - profileCompleted 자동 업데이트
     */
    public LearningProfileResponse updateProfile(Long id, UpdateProfileRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (req.nickname() != null) {
            user.setNickname(req.nickname());
        }
        if (req.major() != null) {
            user.setMajor(req.major());
        }
        if (req.targetJob() != null) {
            user.setTargetJob(req.targetJob());
        }

        /*
         * 📌 update 후 프로필 완료 여부 다시 계산하여 반영
         */
        boolean completed =
                user.getMajor() != null && !user.getMajor().isBlank() &&
                        user.getTargetJob() != null && !user.getTargetJob().isBlank();
        user.setProfileCompleted(completed);

        return toLearningProfileResponse(user);
    }

    /**
     * [5️⃣ 학습프로필 조회]
     * - 기본정보 + 학습프로필 완료 여부
     * - 면접/자소서 학습 이력 전체 요약 리스트 포함
     */
    public LearningProfileResponse getLearningProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return toLearningProfileResponse(user);
    }

    /**
     * [💡 엔티티 → LearningProfileResponse 변환 메서드]
     * - 기본정보 + 학습프로필 완료 여부
     * - 인터뷰/자소서 리스트를 한 번에 세팅
     */
    private LearningProfileResponse toLearningProfileResponse(User user) {

        boolean profileCompleted =
                user.getMajor() != null && !user.getMajor().isBlank() &&
                        user.getTargetJob() != null && !user.getTargetJob().isBlank();

        List<LearningProfileResponse.RecentInterviewSummary> interviewSummaries =
                findInterviewSummaries(user.getId());

        List<LearningProfileResponse.RecentIntroductionSummary> introductionSummaries =
                findIntroductionSummaries(user.getId());

        return LearningProfileResponse.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .major(user.getMajor())
                .targetJob(user.getTargetJob())
                .profileCompleted(profileCompleted)
                .recentInterviews(interviewSummaries)
                .recentIntroductions(introductionSummaries)
                .build();
    }

    /**
     * 🔍 해당 유저의 면접 기록 전체를 요약 리스트로 변환
     * - InterviewLearningRecordRepository.findByUserIdOrderByLearnedAtDesc 사용
     */
    private List<LearningProfileResponse.RecentInterviewSummary> findInterviewSummaries(Long userId) {
        return interviewLearningRecordRepository
                .findByUserIdOrderByLearnedAtDesc(userId)
                .stream()
                .map(record -> {
                    Long interviewId = record.getId();

                    String date = null;
                    if (record.getLearnedAt() != null) {
                        date = record.getLearnedAt().format(DATE_FORMATTER);
                    }

                    return LearningProfileResponse.RecentInterviewSummary.builder()
                            .interviewId(interviewId)
                            .title(null)
                            .score(null)
                            .date(date)
                            .build();
                })
                .toList();
    }

    /**
     * 🔍 해당 유저의 자기소개서 기록 전체를 요약 리스트로 변환
     * - IntroductionLearningHistoryRepository.findByUserIdOrderByLearnedAtDesc 사용
     */
    private List<LearningProfileResponse.RecentIntroductionSummary> findIntroductionSummaries(Long userId) {
        return introductionLearningHistoryRepository
                .findByUserIdOrderByLearnedAtDesc(userId)
                .stream()
                .map(history -> {
                    Long introductionId = history.getIntroduction().getId();

                    String date = null;
                    if (history.getLearnedAt() != null) {
                        date = history.getLearnedAt().format(DATE_FORMATTER);
                    }

                    return LearningProfileResponse.RecentIntroductionSummary.builder()
                            .introductionId(introductionId)
                            .title(null)
                            .date(date)
                            .build();
                })
                .toList();
    }
}