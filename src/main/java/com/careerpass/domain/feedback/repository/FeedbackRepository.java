package com.careerpass.domain.feedback.repository;

import com.careerpass.domain.feedback.entity.Feedback;
import com.careerpass.domain.feedback.entity.FeedbackType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // 자기소개서 ID로 피드백 찾기
    List<Feedback> findByIntroductionIdOrderByIdDesc(Long introductionId);

    // 면접 ID로 피드백 찾기
    List<Feedback> findByInterviewIdOrderByIdDesc(Long interviewId);

    // 피드백 타입으로 검색 (INTRODUCTION / INTERVIEW)
    List<Feedback> findByFeedbackTypeOrderByIdDesc(FeedbackType feedbackType);
}