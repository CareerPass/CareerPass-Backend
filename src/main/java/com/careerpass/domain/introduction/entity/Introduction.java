package com.careerpass.domain.introduction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 자기소개서 엔티티
 * - 사용자(userId)와 연관된 자기소개서 본문 저장
 * - 이후 면접 질문 생성의 기반 데이터로 사용됨
 */
@Entity
@Table(name = "tb_introduction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 프록시 생성용
@AllArgsConstructor
@Builder
@ToString
public class Introduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // wrapper 타입으로 변경: null 가능(신규 저장 시)

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "job_applied", length = 20, nullable = false)
    private String jobApplied;

    @Lob // 자기소개서 본문 길이 가변
    @Column(name = "intro_text", nullable = false, columnDefinition = "LONGTEXT")
    private String introText;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    @Column(name = "submission_time", nullable = false)
    private LocalDateTime submissionTime;

    /**
     * 자기소개서 내용 길이 반환 (필요 시 편의 메서드)
     */
    public int getIntroLength() {
        return introText != null ? introText.length() : 0;
    }
}