package com.careerpass.domain.interview.dto;

import com.careerpass.domain.interview.entity.Interview;
import com.careerpass.domain.interview.entity.Status;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Builder
@AllArgsConstructor @NoArgsConstructor
public class InterviewResponseDto {
    private Long id;
    private Long userId;
    private String jobApplied;
    private String fileUrl;
    private Status status;
    private LocalDateTime requestTime;

    public static InterviewResponseDto from(Interview i) {
        return InterviewResponseDto.builder()
                .id(i.getId())
                .userId(i.getUserId())
                .jobApplied(i.getJobApplied())
                .fileUrl(i.getFileUrl())
                .status(i.getStatus())
                .requestTime(i.getRequestTime())
                .build();
    }
}