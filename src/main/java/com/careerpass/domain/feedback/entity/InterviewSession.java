package com.careerpass.domain.feedback.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "tb_interview_session")
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "job_applied", nullable = false, length = 100)
    private String jobApplied;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "total_duration_sec")
    private Long totalDurationSec;

    @Column(name = "question_count")
    private Integer questionCount;

    @Column(name = "average_score")
    private Double averageScore;

    @Column(name = "overall_strengths", columnDefinition = "TEXT")
    private String overallStrengths;

    @Column(name = "overall_improvements", columnDefinition = "TEXT")
    private String overallImprovements;

    @Column(name = "overall_risks", columnDefinition = "TEXT")
    private String overallRisks;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (title == null || title.isBlank()) {
            title = "면접";
        }
        if (status == null) {
            status = SessionStatus.IN_PROGRESS;
        }
    }
}
