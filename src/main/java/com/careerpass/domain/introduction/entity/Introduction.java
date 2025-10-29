package com.careerpass.domain.introduction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "tb_introduction")
public class Introduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Lob
    @Column(name = "intro_text", nullable = false)
    private String introText;

    @Column(name = "job_applied", length = 20, nullable = false)
    private String jobApplied;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    @Column(name = "submission_time", nullable = false)
    private LocalDateTime submissionTime;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}
