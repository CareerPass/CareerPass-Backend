package com.careerpass.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 학습 프로필 응답 DTO
@Getter
@Builder
public class LearningProfileResponse {

    // 1. 기본 정보
    private String nickname;       // 사용자 이름
    private String email;      // 구글에서 들고온 이메일
    private String major;      // 전공
    private String targetJob;  // 목표 직무

    // 학습프로필 완료 여부 (major + targetJob 둘 다 있으면 true)
    private boolean profileCompleted;

    // 2. 최근 면접 기록 요약들 (없으면 빈 리스트 or null)
    private List<RecentInterviewSummary> recentInterviews;

    // 3. 최근 자기소개서 기록 요약들 (없으면 빈 리스트 or null)
    private List<RecentIntroductionSummary> recentIntroductions;

    @Getter
    @Builder
    public static class RecentInterviewSummary {
        private Long interviewId;   // 상세 페이지 들어갈 때 쓸 수도 있으니까
        private String title;       // “네이버” 같은 회사/면접 이름
        private Double score;       // 89점
        private String date;        // "2024.12.18" 처럼 포맷해서 내려줄 문자열
    }

    @Getter
    @Builder
    public static class RecentIntroductionSummary {
        private Long introductionId; // 상세 보기용
        private String title;        // 예: “네이버 백엔드 1번 자기소개서”
        private String date;         // "2024.12.18" 포맷 문자열
    }
}