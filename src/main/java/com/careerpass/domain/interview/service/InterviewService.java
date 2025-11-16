package com.careerpass.domain.interview.service;

import com.careerpass.domain.interview.entity.Interview;
import com.careerpass.domain.interview.entity.Status;
import com.careerpass.domain.interview.repository.InterviewJpaRepository;
import com.careerpass.global.aws.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 🎤 인터뷰 저장 서비스
 * - 파일 검증 → S3 업로드 → tb_interview 저장
 * - S3/IO 계열 예외는 런타임으로 변환하여 전역 예외 핸들러가 처리하도록 함
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {

    private final InterviewJpaRepository interviewJpaRepository;
    private final S3Service s3Service;

    /**
     * 컨트롤러에서 호출하는 저장 로직
     * @param userId     사용자 ID (>=1)
     * @param jobApplied 지원 직무
     * @param audioFile  업로드된 음성 파일
     * @return 저장된 Interview 엔티티
     */
    @Transactional
    public Interview createInterview(Long userId, String jobApplied, MultipartFile audioFile) {
        // 1) 입력 검증
        validateAudio(audioFile);
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 userId 입니다.");
        }
        if (jobApplied == null || jobApplied.isBlank()) {
            throw new IllegalArgumentException("지원 직무(jobApplied)는 비어 있을 수 없습니다.");
        }

        // 2) S3 업로드 (checked 예외는 여기서 런타임으로 변환)
        final String fileLocation;
        try {
            // s3Service.storeFile(...)이 IOException 등을 던져도 여기서 잡아서 변환
            fileLocation = s3Service.storeFile(audioFile); // 반환: S3 key 또는 URL (정책에 맞게 사용)
        } catch (Exception e) {
            log.error("S3 업로드 실패: {}", e.getMessage(), e);
            throw new IllegalStateException("S3 업로드 중 오류가 발생했습니다.", e);
        }

        // 3) 엔티티 생성/저장
        Interview interview = Interview.builder()
                .userId(userId)
                .jobApplied(jobApplied)
                .fileUrl(fileLocation)      // 권장: 'S3 key' 만 저장
                .status(Status.BEFANALYSE)  // 프로젝트에 존재하는 enum 값 사용
                .requestTime(LocalDateTime.now())
                .finishTime(null)
                .build();

        return interviewJpaRepository.save(interview);
    }

    // ==============
    // 내부 유틸
    // ==============
    private void validateAudio(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new IllegalArgumentException("업로드된 음성 파일이 비어 있습니다.");
        }
        if (Objects.isNull(audioFile.getOriginalFilename())) {
            log.warn("audio file has no original filename");
        }
        String filename = audioFile.getOriginalFilename();
        if (filename != null && !filename.matches(".*\\.(wav|mp3|m4a|webm|ogg)$")) {
            throw new IllegalArgumentException("지원하지 않는 오디오 형식입니다. (허용: wav, mp3, m4a, webm, ogg)");
        }
    }
}