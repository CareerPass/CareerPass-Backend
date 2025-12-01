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

    // Python AnswerDispatch
    public record AnswerDispatchDto(
            Long answerId,
            String questionText,
            String transcript,
            String resumeContent,
            InterviewMetaDto meta
    ) {}

    // Python AnswerAnalysisResult
    public record AnswerAnalysisResultDto(
            Integer score,
            Long timeMs,
            Integer fluency,
            Integer contentDepth,
            Integer structure,
            Integer fillerCount,
            List<String> improvements,
            List<String> strengths,
            List<String> risks
    ) {}
}