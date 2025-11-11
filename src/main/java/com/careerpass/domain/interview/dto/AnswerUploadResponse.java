package com.careerpass.domain.interview.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AnswerUploadResponse {
    private String audioUrl;    // 저장된 파일 접근 경로/S3 URL
    private Long questionId;
}