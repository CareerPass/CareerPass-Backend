package com.careerpass.domain.interview.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class StartInterviewResponse {
    private Long interviewId;                // 세션/시도 구분용 ID
    private List<QuestionItemDto> items;     // 확정된 질문 목록
}
