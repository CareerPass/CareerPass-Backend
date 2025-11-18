package com.careerpass.domain.introduction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 자소서 기반 AI 면접 질문 엔티티
 * - Introduction(자소서)을 기반으로 생성된 질문을 저장
 * - GenerateQuestionsResponse / QuestionItemDto에서 넘어온 데이터 매핑용
 */
@Entity
@Table(name = "tb_introduction_question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString(exclude = "introduction")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 어떤 자기소개서(Introduction)에서 생성된 질문인지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "introduction_id", nullable = false)
    private Introduction introduction;

    /**
     * 실제 질문 내용
     */
    @Lob
    @Column(name = "question_text", nullable = false, columnDefinition = "LONGTEXT")
    private String questionText;

    /**
     * 해당 자소서에서의 질문 순서 (1, 2, 3, ...)
     */
    @Column(name = "order_index")
    private Integer orderIndex;

    /**
     * 질문 생성 시점의 전공 / 직무 정보
     * (GenerateQuestionsResponse.major / jobTitle 그대로 저장)
     */
    @Column(name = "major", length = 50)
    private String major;

    @Column(name = "job_title", length = 50)
    private String jobTitle;

    /**
     * 질문이 생성된 시각
     * - GenerateQuestionsResponse.generatedAt 기준
     * - OffsetDateTime → LocalDateTime 변환해서 저장해도 됨(UTC 기준 or KST 기준 선택)
     */
    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    // ===== 편의 메서드 =====

    public void changeOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public void changeIntroduction(Introduction introduction) {
        this.introduction = introduction;
    }
}