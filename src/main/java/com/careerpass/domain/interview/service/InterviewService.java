package com.careerpass.domain.interview.service;

import com.careerpass.domain.interview.entity.Interview;
import com.careerpass.domain.interview.entity.Status;
import com.careerpass.domain.interview.repository.InterviewJpaRepository;
import com.careerpass.global.aws.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InterviewService {
    private final InterviewJpaRepository interviewJpaRepository;
    private final S3Service s3Service;

    @Transactional
    public Interview createInterview(Long userId, String jobApplied, MultipartFile audioFile) throws IOException {
        String fileS3Key = s3Service.storeFile(audioFile);

        Interview interview = Interview.builder()
                .fileUrl(fileS3Key)
                .status(Status.BEFANALYSE)
                .requestTime(LocalDateTime.now())
                .jobApplied(jobApplied)
                .userId(userId)
                .build();

        return interviewJpaRepository.save(interview);
    }
}
