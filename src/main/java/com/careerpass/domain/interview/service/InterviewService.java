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
    // private final AIService aiService; // ✅ 향후 자동 분석 연동용 (지금은 주석 처리)

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


        Interview saved = interviewJpaRepository.save(interview);

        // ✅ (선택) 자동 AI 분석 연동
        /*
        try {
            AnalysisResultDto analysis = aiService.analyzeVoice(
                new AnswerUploadMetaDto(saved.getId(), null), audioFile);
            saved.setStatus(Status.FINISH);
            interviewJpaRepository.save(saved);
        } catch (Exception e) {
            log.warn("AI 분석 실패 (저장만 완료): {}", e.getMessage());
        }
        */

        return saved;
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
            throw new IllegalArgumentException("❌ 업로드된 음성 파일이 비어 있습니다.");
        }

        if (Objects.isNull(audioFile.getOriginalFilename())) {
            log.warn("⚠️ audio file has no original filename");
        }

        // ⚠️ 필요 시 파일 형식 제한 추가
        String filename = audioFile.getOriginalFilename();
        if (filename != null && !filename.matches(".*\\.(wav|mp3|m4a)$")) {
            throw new IllegalArgumentException("❌ 지원하지 않는 오디오 형식입니다. (허용: wav, mp3, m4a)");
        }
    }
}