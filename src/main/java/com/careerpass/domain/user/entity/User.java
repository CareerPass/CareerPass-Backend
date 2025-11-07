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

    @Column(name = "major", length = 20, nullable = true)
    private String major;

    @Column(name = "target_job", length = 20, nullable = true)
    private String targetJob;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type", nullable = false, length = 20)
    private SocialType socialType;

    @Column(name = "social_number", nullable = false, length = 64)
    private String socialNumber;
}
