package com.careerpass.domain.interview.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "tb_interview")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_url", length = 255, nullable = false)
    private String fileUrl;

    @Column(name = "status", nullable = false)
    private Status status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    @Column(name = "request_time", nullable = false)
    private LocalDateTime requestTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    @Column(name = "finish_time", nullable = true)
    private LocalDateTime finishTime;

    @Column(name = "job_applied", nullable = false)
    private String jobApplied;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}
