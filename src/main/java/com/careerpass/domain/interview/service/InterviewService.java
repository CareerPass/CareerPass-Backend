package com.careerpass.domain.interview.service;

import com.careerpass.domain.interview.dto.AnswerUploadMetaDto;
import com.careerpass.domain.interview.dto.AnswerUploadResponse;
import com.careerpass.domain.interview.entity.Interview;
import com.careerpass.domain.interview.entity.Status;
import com.careerpass.domain.interview.repository.InterviewJpaRepository;
import com.careerpass.global.aws.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {

    private final InterviewJpaRepository interviewJpaRepository;
    private final S3Service s3Service;

    // =========================
    // 방법 1) 기존 컨트롤러 호환용
    // =========================
    /**
     * 컨트롤러에서 호출 중인 기존 시그니처 유지:
     * createInterview(userId, jobApplied, file)
     *
     * - S3 업로드
     * - Interview 엔티티 생성/저장
     * - 상태는 현재 enum에 존재하는 값 사용: Status.BEFANALYSE
     */
    @Transactional
    public Interview createInterview(Long userId, String jobApplied, MultipartFile audioFile) throws IOException {
        validateAudio(audioFile);

        // S3 업로드 (반환값: URL 또는 Key — 현재는 fileUrl 필드에 그대로 저장)
        final String fileLocation = s3Service.storeFile(audioFile);

        Interview interview = Interview.builder()
                .userId(userId)
                .jobApplied(jobApplied)
                .fileUrl(fileLocation)
                .status(Status.BEFANALYSE)          // 프로젝트에 실제 존재하는 enum 값 사용
                .requestTime(LocalDateTime.now())
                .build();

        return interviewJpaRepository.save(interview);
    }

    // ===========================================
    // 보너스) DTO(meta+file) 신시그니처(향후 전환용)
    // ===========================================
    /**
     * meta: { interviewId, questionId } 기반 업로드
     * 컨트롤러를 meta+file로 바꿀 때 사용할 버전.
     */
    @Transactional
    public AnswerUploadResponse uploadAnswer(AnswerUploadMetaDto meta, MultipartFile audioFile) throws IOException {
        validateAudio(audioFile);

        // 인터뷰 존재 확인
        Interview interview = interviewJpaRepository.findById(meta.getInterviewId())
                .orElseThrow(() -> new IllegalArgumentException("interview not found: " + meta.getInterviewId()));

        final String fileLocation = s3Service.storeFile(audioFile);

        // 파일/상태 갱신
        interview.setFileUrl(fileLocation);
        interview.setStatus(Status.BEFANALYSE);
        interview.setRequestTime(LocalDateTime.now());
        interviewJpaRepository.save(interview);

        return AnswerUploadResponse.builder()
                .audioUrl(fileLocation)
                .questionId(meta.getQuestionId())
                .build();
    }

    // ==============
    // 내부 유틸
    // ==============
    private void validateAudio(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new IllegalArgumentException("empty audio file");
        }
        // 필요하면 용량/콘텐츠 타입 검증 추가:
        // if (audioFile.getSize() > 25L * 1024 * 1024) { ... }
        // if (!Objects.equals(audioFile.getContentType(), "audio/wav")) { ... }
        if (Objects.isNull(audioFile.getOriginalFilename())) {
            log.warn("audio file has no original filename");
        }
    }
}