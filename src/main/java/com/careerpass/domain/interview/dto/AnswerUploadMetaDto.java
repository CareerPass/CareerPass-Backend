package com.careerpass.domain.interview.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 🎙️ 음성 업로드 메타데이터 DTO
 * - multipart/form-data 요청 시 함께 전달되는 JSON 파트(meta)
 * - 각 음성 파일이 어떤 질문에 대한 답변인지 식별하기 위해 사용됨
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerUploadMetaDto {

    /** 인터뷰 시도 ID (한 세션 단위 식별용) */
    @NotNull private Long interviewId;

    /** 질문 ID (QuestionItemDto.questionId와 매칭됨) */
    @NotNull private String questionId;

    /** (옵션) 클라이언트 측 녹음 시작 시각 (ms 단위) */
    private Long clientTimestamp;
}