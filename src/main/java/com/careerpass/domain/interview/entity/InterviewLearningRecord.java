package com.careerpass.domain.interview.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 모의면접 학습 기록 엔티티
 * - 자소서 기반으로 생성된 질문(Question)에 대해
 *   사용자가 음성 답변을 하고, 그 결과를 저장하는 테이블
 */
@Entity
@Table(name = "tb_interview_learning_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class InterviewLearningRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 학습을 진행한 사용자 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 어떤 질문에 대한 학습인지
     * - introduction 도메인의 Question 엔티티를 그대로 참조
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /**
     * 사용자가 녹음한 음성 파일 URL (S3 등)
     */
    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    /**
     * Whisper로 변환된 답변 텍스트
     */
    @Lob
    @Column(name = "answer_text", columnDefinition = "LONGTEXT")
    private String answerText;

    /**
     * AI 분석 결과 (피드백, 점수, 코멘트 등)
     */
    @Lob
    @Column(name = "analysis_result", columnDefinition = "LONGTEXT")
    private String analysisResult;

    /**
     * 답변 소요 시간(ms)
     */
    @Column(name = "duration_ms")
    private Long durationMs;

    /**
     * 학습이 저장된 시각
     */
    @Column(name = "learned_at", nullable = false)
    private LocalDateTime learnedAt;

    @PrePersist
    public void onCreate() {
        this.learnedAt = LocalDateTime.now();
    }
}