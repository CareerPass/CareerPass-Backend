// InterviewAiDtos.java

package com.careerpass.domain.feedback.dto;

import java.util.List;

public class InterviewAiDtos {

    // Python InterviewMeta
    public record InterviewMetaDto(
            Long id,
            Long userId,
            String jobApplied,
            Long questionId
    ) {}

    // 💡 1-1. STT 요청 시 파일과 함께 받을 메타데이터 DTO 추가
    public record SttRequestMetaDto(
            Long interviewId,      // InterviewMetaDto의 id와 동일
            Long questionId,
            String questionText,   // 질문 텍스트
            String resumeContent,  // 이력서/자기소개서 내용
            String jobApplied,      // 직무 정보 (InterviewMetaDto 구성용)
            Long interviewDuration
    ) {}

    // 💡 1-2. STT 서버 응답 DTO (voice_ai.SttResult와 매핑)
    public record SttResultDto(
            String answerText
    ) {}


    public record AnswerDispatchDto(
            Long answerId,
            String questionText,
            String transcript,
            Long interviewDuration,
            String resumeContent,
            InterviewMetaDto meta
    ) {}

    public record AnswerAnalysisResultDto(
            String transcript,
            Integer score,
            Long timeMs,
            List<String> improvements,
            List<String> strengths,
            List<String> risks
    ) {}
}
