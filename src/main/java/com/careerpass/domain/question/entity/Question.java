package com.careerpass.domain.question.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "tb_question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Column(name = "introduction_id", nullable = false)
    private Long introduction_id;
}
