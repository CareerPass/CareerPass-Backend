package com.careerpass.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// 학습 프로필 응답 DTO (Feedback 기반 요약)
@Getter
@Builder
public class LearningProfileResponse {

    private Long id;             // 사용자 ID
    private String nickname;     // 사용자 이름
    private String email;        // 이메일
    private String major;        // 전공
    private String targetJob;    // 목표 직무
    private boolean profileCompleted;

    // 피드백 요약 목록
    private List<FeedbackSummary> introductionFeedbacks;
    private List<FeedbackSummary> interviewFeedbacks;

    @Getter
    @Builder
    public static class FeedbackSummary {
        private Long id;               // feedbackId
        private String title;          // 피드백 제목
        private Long totalScore;       // 점수 (null 가능)
        private LocalDateTime createdAt; // 생성 시각
    }
}
