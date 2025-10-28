package com.careerpass.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "tb_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", length = 50, nullable = false)
    private String email;

    @Column(name = "nickname", length = 16, nullable = false)
    private String nickname;

    @Column(name = "grade", nullable = true)
    private Long grade;

    @Column(name = "major", length = 20, nullable = true)
    private String major;

    @Column(name = "target_job", length = 20, nullable = true)
    private String targetJob;

    @Column(name = "student_number", nullable = true)
    private Long studentNumber;

    @Column(name = "social_type", nullable = false)
    private SocialType socialType;

    @Column(name = "social_number", nullable = false)
    private String socialNumber;
}
