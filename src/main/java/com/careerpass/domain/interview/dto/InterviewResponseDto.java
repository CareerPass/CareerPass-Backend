package com.careerpass.domain.interview.dto;

import com.careerpass.domain.interview.entity.Interview;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InterviewResponseDto {
    private Long id;
    private String fileUrl;    // S3 key 또는 presigned URL (정책에 따라)
    private String status;
    private String jobApplied;
    private Long userId;
    private LocalDateTime requestTime;
    private LocalDateTime finishTime;

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