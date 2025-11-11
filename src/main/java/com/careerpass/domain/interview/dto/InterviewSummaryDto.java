package com.careerpass.domain.interview.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class InterviewSummaryDto {
    private Long interviewId;
    private Double averageScore;                 // 전체 평균
    private List<AnalysisResultDto> results;     // 문항별 결과
}