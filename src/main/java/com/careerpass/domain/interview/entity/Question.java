package com.careerpass.domain.interview.entity;

import com.careerpass.domain.introduction.entity.Introduction;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_interview_question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 질문 텍스트 (AI가 생성한 실제 질문)
     */
    @Lob
    @Column(name = "question_text", nullable = false, columnDefinition = "LONGTEXT")
    private String questionText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "introduction_id")
    private Introduction introduction;

    /**
     * 질문 순서 (1, 2, 3...)
     */
    @Column(name = "order_index")
    private Integer orderIndex;

    /**
     * 전공 / 직무 정보 저장
     */
    @Column(length = 30)
    private String major;

    @Column(length = 30)
    private String jobTitle;
}