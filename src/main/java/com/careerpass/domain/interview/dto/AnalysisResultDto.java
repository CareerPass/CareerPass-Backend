package com.careerpass.domain.interview.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalysisResultDto {
    private Long questionId;
    private String question;          // 원문 질문(프론트 표시용)
    private String answerText;        // 최종 텍스트(전사 + 정제)
    private Double score;             // 0.0 ~ 10.0 등급
    private String feedback;          // 개선 코멘트
    private Long durationMs;          // 답변 시간
    private List<String> keywords;    // 포함/미포함 핵심 키워드
}