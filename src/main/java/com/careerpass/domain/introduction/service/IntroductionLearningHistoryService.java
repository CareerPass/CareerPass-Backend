package com.careerpass.domain.introduction.service;

import com.careerpass.domain.introduction.entity.Introduction;
import com.careerpass.domain.introduction.entity.IntroductionLearningHistory;
import com.careerpass.domain.introduction.repository.IntroductionLearningHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 📘 자소서 기반 학습 기록 서비스
 *
 * - 사용자가 특정 자소서를 기반으로 질문 학습을 진행했을 때
 *   해당 학습 이력을 저장하는 역할 수행
 * - Controller와 Repository 사이의 비즈니스 로직을 담당하는 계층
 */
@Service
@RequiredArgsConstructor
public class IntroductionLearningHistoryService {

    private final IntroductionLearningHistoryRepository historyRepository;

    /**
     * 🔹 자소서 학습 기록 저장
     *
     * @param userId        학습을 진행한 사용자의 ID
     * @param introduction  어떤 자소서를 기반으로 했는지 (FK)
     * @param questionCount 이번 학습 세션에서 사용된 질문 개수
     * @return 저장된 IntroductionLearningHistory 엔티티
     */
    public IntroductionLearningHistory saveHistory(
            Long userId,
            Introduction introduction,
            int questionCount
    ) {
        // 엔티티 생성 (Builder 패턴 사용)
        IntroductionLearningHistory history = IntroductionLearningHistory.builder()
                .userId(userId)
                .introduction(introduction)
                .questionCount(questionCount)
                .build();

        // DB 저장 후 결과 반환
        return historyRepository.save(history);
    }
}