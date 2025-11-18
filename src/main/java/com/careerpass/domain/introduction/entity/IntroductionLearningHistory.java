package com.careerpass.domain.introduction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 자기소개서 기반 학습 기록 테이블
 * - 사용자가 특정 자소서를 기반으로 학습했을 때 저장하는 기록
 */
@Entity
@Table(name = "tb_introduction_learning_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class IntroductionLearningHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 학습을 진행한 사용자 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 어떤 자소서(Introduction)를 기반으로 했는지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "introduction_id", nullable = false)
    private Introduction introduction;

    /**
     * 이번 학습에서 사용된 질문 개수
     */
    @Column(name = "question_count", nullable = false)
    private int questionCount;

    /**
     * 학습 진행 시간
     */
    @Column(name = "learned_at", nullable = false)
    private LocalDateTime learnedAt;

    @PrePersist
    public void onCreate() {
        this.learnedAt = LocalDateTime.now();
    }
}