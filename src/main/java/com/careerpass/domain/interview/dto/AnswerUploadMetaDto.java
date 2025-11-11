package com.careerpass.domain.interview.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AnswerUploadMetaDto {
    @NotNull
    private Long interviewId;     // 어떤 시도에 대한 답변인지

    @NotNull
    private Long questionId;      // 어떤 질문의 답변인지

    private Long clientTimestamp; // (옵션) 클라이언트 측 녹음 시작 시각(ms)
}