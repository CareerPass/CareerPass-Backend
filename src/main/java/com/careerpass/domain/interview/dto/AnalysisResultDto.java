package com.careerpass.domain.interview.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalysisResultDto {

    @JsonProperty("questionId")
    private Long questionId;

    @JsonProperty("question")
    private String question;          // 원문 질문(프론트 표시용)

    @JsonProperty("answerText")
    private String answerText;        // 최종 텍스트(전사 + 정제)

    @JsonProperty("score")
    private Double score;             // 0.0 ~ 10.0 등급

    @JsonProperty("feedback")
    private String feedback;          // 개선 코멘트

    @JsonProperty("durationMs")
    private Long durationMs;          // 답변 시간

    @JsonProperty("keywords")
    private List<String> keywords;    // 포함/미포함 핵심 키워드
}