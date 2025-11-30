package com.careerpass.domain.user.service;

import com.careerpass.domain.introduction.repository.IntroductionLearningHistoryRepository;
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
     * [0️⃣ 로그인 or 자동 가입]
     * - 프론트가 구글 OAuth로 받아온 email만 전달
     * - 이미 존재하면 → 그대로 프로필 반환
     * - 없으면      → email 기반 기본 닉네임으로 유저 생성 후 프로필 반환
     */
    public LearningProfileResponse loginOrCreateByEmail(String email) {

        // 1) 이미 존재하면 그대로 반환
        return userRepository.findByEmail(email)
                .map(this::toLearningProfileResponse)
                .orElseGet(() -> {
                    // 2) 없으면 새 유저 생성

                    // 닉네임 기본값: 이메일 앞부분
                    String defaultNickname;
                    if (email != null && email.contains("@")) {
                        defaultNickname = email.substring(0, email.indexOf("@"));
                    } else {
                        defaultNickname = "user";
                    }

                    User user = User.builder()
                            .email(email)
                            .nickname(defaultNickname)   // 로그인 직후 바로 보여줄 닉네임
                            .major(null)                // 미설정
                            .targetJob(null)            // 미설정
                            .profileCompleted(false)    // 처음엔 무조건 false
                            .socialType(SocialType.GOOGLE)
                            .socialNumber("GOOGLE-" + UUID.randomUUID())
                            .build();

                    userRepository.save(user);
                    return toLearningProfileResponse(user);
                });
    }

    /**
     * [1️⃣ 사용자 생성]
     * - Swagger 등에서 직접 테스트할 때 사용 (email 기반 생성)
     * - 이메일이 이미 존재하면 DuplicateEmailException(409)
     */
    public LearningProfileResponse create(CreateUserRequest req) {

        String email = req.email();

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        User user = User.builder()
                .email(email)
                .nickname(null)
                .major(null)
                .targetJob(null)
                .profileCompleted(false)
                .socialType(SocialType.GOOGLE)
                .socialNumber("GOOGLE-" + UUID.randomUUID())
                .build();

        userRepository.save(user);

        return toLearningProfileResponse(user);
    }

    /**
     * [2️⃣ 단일 조회 - id 기준]
     */
    public LearningProfileResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return toLearningProfileResponse(user);
    }

    /**
     * [2-2️⃣ 단일 조회 - email 기준]
     * - /me 같은 곳에서 사용 가능
     */
    public LearningProfileResponse getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("email=" + email));

        return toLearningProfileResponse(user);
    }

    /**
     * [3️⃣ 전체 조회]
     */
    public List<LearningProfileResponse> getAll() {
        return userRepository.findAll().stream()
                .map(this::toLearningProfileResponse)
                .toList();
    }

    /**
     * [4️⃣ 프로필 수정]
     * - 이메일 제외 (닉네임, 전공, 목표 직무만 수정 가능)
     * - 닉네임 + 전공 + 목표 직무가 모두 채워져야 profileCompleted = true
     */
    public LearningProfileResponse updateProfile(Long id, UpdateProfileRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (req.nickname() != null) {
            user.setNickname(req.nickname());
        }
        if (req.major() != null) {
            user.setMajor(req.major());
        }
        if (req.targetJob() != null) {
            user.setTargetJob(req.targetJob());
        }

        // 수정 후 profileCompleted 다시 계산 (닉네임 + 전공 + 목표 직무 모두 필요)
        boolean completed = isProfileCompleted(user);
        user.setProfileCompleted(completed);

        userRepository.save(user);

        return toLearningProfileResponse(user);
    }

    /**
     * [5️⃣ 학습프로필 조회]
     */
    public LearningProfileResponse getLearningProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return toLearningProfileResponse(user);
    }

    /**
     * [💡 엔티티 → LearningProfileResponse 변환 메서드]
     * - 여기서도 동일 기준으로 profileCompleted 계산
     */
    private LearningProfileResponse toLearningProfileResponse(User user) {
        boolean profileCompleted = isProfileCompleted(user);

        List<LearningProfileResponse.RecentInterviewSummary> interviewSummaries =
                findInterviewSummaries(user.getId());

        List<LearningProfileResponse.RecentIntroductionSummary> introductionSummaries =
                findIntroductionSummaries(user.getId());

        return LearningProfileResponse.builder()
                .id(user.getId())
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
     * 학습 프로필 완료 여부 계산
     * - 닉네임, 전공, 목표 직무가 모두 null/빈문자열이 아니어야 함
     */
    private boolean isProfileCompleted(User user) {
        return isNotBlank(user.getNickname())
                && isNotBlank(user.getMajor())
                && isNotBlank(user.getTargetJob());
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

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
                            .title(null)   // 필요 시 record에서 제목 필드 꺼내기
                            .score(null)   // 필요 시 record에서 점수 필드 꺼내기
                            .date(date)
                            .build();
                })
                .toList();
    }

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
                            .title(null)   // 필요 시 제목 필드 매핑
                            .date(date)
                            .build();
                })
                .toList();
    }
}