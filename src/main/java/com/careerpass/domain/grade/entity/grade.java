package com.careerpass.domain.grade.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "tb_grade")
public class grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_name", length = 20, nullable = false)
    private String courseName;

    @Column(name = "grade", nullable = true)
    private String grade;

    @Column(name = "user_id", nullable = false)
    private Long userId;

}
