package com.careerpass.domain.interview.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TranscribeResultDto {
    private Long questionId;
    private String transcript;    // 인식된 텍스트
    private Long durationMs;      // 답변 길이(ms) - 프론트/서버 계산 중 편한 쪽
    private Double confidence;    // (옵션) 엔진 신뢰도
}