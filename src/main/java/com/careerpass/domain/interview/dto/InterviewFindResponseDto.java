package com.careerpass.domain.interview.dto;

import com.careerpass.domain.interview.entity.Interview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewFindResponseDto {

    private String fileUrl;
    private String jobApplied;

    public static InterviewFindResponseDto toDto(Interview interview) {
        if (interview == null) {
            return null;
        }else {
            return InterviewFindResponseDto.builder()
                    .fileUrl(interview.getFileUrl())
                    .jobApplied(interview.getJobApplied())
                    .build();
        }
    }
}
