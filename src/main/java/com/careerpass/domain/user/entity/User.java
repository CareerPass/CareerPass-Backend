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

    @Column(name = "profile_completed", nullable = false)
    private boolean profileCompleted;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type", nullable = false, length = 20)
    private SocialType socialType;

    @Column(name = "social_number", nullable = false, length = 64)
    private String socialNumber;

    public void updateProfile(String nickname, String major, String targetJob) {
        this.nickname = nickname;
        this.major = major;
        this.targetJob = targetJob;

        // 이메일은 바꾸지 않음
        // 소셜 정보도 절대 변경하면 안 됨

        // 프로필 완성 여부 체크
        this.profileCompleted =
                isNotBlank(this.nickname) &&
                        isNotBlank(this.major) &&
                        isNotBlank(this.targetJob);
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
