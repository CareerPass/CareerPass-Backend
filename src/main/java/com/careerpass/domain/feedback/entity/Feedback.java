package com.careerpass.domain.feedback.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "tb_feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "feedback_type", nullable = false)
    private FeedbackType feedbackType;

    @Column(name = "total_score", nullable = true)
    private Long totalScore;

    @Column(name = "transcript", columnDefinition = "TEXT", nullable = true)
    private String transcript;

    @Lob
    @Column(name = "feedback_text", columnDefinition = "LONGTEXT", nullable = false)
    private String feedbackText;

    @Column(name = "section_feedback", columnDefinition = "TEXT", nullable = false)
    private String sectionFeedback;

    @Column(name = "introduction_id", nullable = true)
    private Long introductionId;

    @Column(name = "interview_id", nullable = true)
    private Long interviewId;

    @Column(name = "question_id", nullable = true)
    private Long questionId;

    @Column(name = "audio_url", nullable = true)
    private String audioUrl;

    @Column(name = "duration_ms", nullable = true)
    private Long durationMs;

    @Column(name = "created_at", nullable = false)
    private java.time.LocalDateTime createdAt;

    @PrePersist
    public void validateAssociation() {
        if (feedbackType == null) {
            throw new IllegalStateException("feedbackType이 null일 수 없습니다.");
        }
        if (createdAt == null) {
            createdAt = java.time.LocalDateTime.now();
        }
    }
}
