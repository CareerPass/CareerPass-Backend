package com.careerpass.domain.introduction.repository;

import com.careerpass.domain.introduction.entity.IntroductionLearningHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 자기소개서 학습 기록 레포지토리
 * - 특정 사용자 / 자소서 기준으로 학습 이력 조회 가능
 */
public interface IntroductionLearningHistoryRepository
        extends JpaRepository<IntroductionLearningHistory, Long> {

    // 특정 유저의 전체 자소서 학습 기록
    List<IntroductionLearningHistory> findByUserIdOrderByLearnedAtDesc(Long userId);

    // 특정 자소서에 대한 학습 기록
    List<IntroductionLearningHistory> findByIntroductionIdOrderByLearnedAtDesc(Long introductionId);
}