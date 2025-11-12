package com.careerpass.domain.interview.dto;

import com.careerpass.domain.interview.entity.Interview;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@Schema(description = "면접 업로드 응답 DTO")
public class InterviewResponseDto {
    @Schema(example = "1") private Long id;
    @Schema(example = "https://s3.aws.com/interview/file123.m4a") private String fileUrl;
    @Schema(example = "BEFANALYSE") private String status;
    @Schema(example = "백엔드 개발자") private String jobApplied;
    @Schema(example = "5") private Long userId;
    @Schema(example = "2025-11-12T21:35:00") private LocalDateTime requestTime;
    @Schema(example = "null") private LocalDateTime finishTime;

    public static InterviewResponseDto from(Interview i) {
        return InterviewResponseDto.builder()
                .id(i.getId())
                .fileUrl(i.getFileUrl())
                .status(i.getStatus().name())
                .jobApplied(i.getJobApplied())
                .userId(i.getUserId())
                .requestTime(i.getRequestTime())
                .finishTime(i.getFinishTime())
                .build();
    }
}