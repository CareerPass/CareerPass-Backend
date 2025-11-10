package com.careerpass.domain.interview.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class StartInterviewRequest {
    private Long userId;                 // 응시자
    private List<String> questions;      // 외부 API에서 받은 질문 텍스트 리스트
}