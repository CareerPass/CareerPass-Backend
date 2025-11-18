package com.careerpass.domain.interview.repository;

import com.careerpass.domain.interview.entity.InterviewLearningRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 모의면접 학습 기록 레포지토리
 * - 질문별 / 유저별 학습 이력 조회
 */
public interface InterviewLearningRecordRepository
        extends JpaRepository<InterviewLearningRecord, Long> {

    // 특정 질문에 대한 모든 학습 기록
    List<InterviewLearningRecord> findByQuestionIdOrderByLearnedAtDesc(Long questionId);

    // 특정 유저의 전체 모의면접 학습 기록
    List<InterviewLearningRecord> findByUserIdOrderByLearnedAtDesc(Long userId);
}