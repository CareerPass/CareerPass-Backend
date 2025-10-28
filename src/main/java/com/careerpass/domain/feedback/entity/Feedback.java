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

    @Column(name = "feedback_type", nullable = false)
    private FeedbackType feedbackType;

    @Column(name = "total_score", nullable = false)
    private Long totalScore;

    @Lob
    @Column(name = "feedback_text", nullable = false)
    private String feedbackText;

    @Column(name = "section_feedback", columnDefinition = "TEXT", nullable = false)
    private String sectionFeedback;

    @Column(name = "introduction_id", nullable = true)
    private Long introductionId;

    @Column(name = "interview_id", nullable = true)
    private Long interviewId;


    @PrePersist
    @PreUpdate
    public void validateAssociation() {
        if(feedbackType == FeedbackType.INTRODUCTION) {
            if(introductionId == null || interviewId != null) {
                throw new IllegalStateException("자소서 id가 필요하고, 면접 id는 NULL이어야 한다.");
            }
        }else if(feedbackType == FeedbackType.INTERVIEW) {
            if(interviewId == null || introductionId != null) {
                throw new IllegalStateException("면접 id가 필요하고, 자소서 id는 NULL이어야 한다.");
            }
        }else {
            throw new IllegalStateException("feedbackType이 INTRODUCTION 또는 INTERVIEW이어야 한다.");
        }
    }
}
