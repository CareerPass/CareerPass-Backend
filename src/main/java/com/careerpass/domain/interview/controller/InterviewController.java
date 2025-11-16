package com.careerpass.domain.interview.controller;

import com.careerpass.domain.interview.dto.InterviewResponseDto;
import com.careerpass.domain.interview.entity.Interview;
import com.careerpass.domain.interview.service.InterviewService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 🎤 인터뷰 저장 전용 컨트롤러
 * - 음성 파일을 S3에 저장하고, tb_interview에 메타데이터를 기록한다.
 * - AI 분석(전사/점수)은 AIController가 담당한다.
 */
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
@Slf4j
@Validated
public class InterviewController {

    private final InterviewService interviewService;

    /**
     * [POST] /api/interview/audio
     * form-data:
     *  - file (MultipartFile, required)
     *  - userId (Long, required, >=1)
     *  - jobApplied (String, required)
     *
     * 반환: 201 Created + InterviewResponseDto
     * 실패: GlobalExceptionHandler에서 400/500 응답
     */
    @PostMapping(
            value = "/audio",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<InterviewResponseDto> registerInterview(
            @RequestPart("file") MultipartFile file,
            @RequestParam("userId") @NotNull @Positive Long userId,
            @RequestParam("jobApplied") @NotBlank String jobApplied
    ) {
        // 최소 방어 (서비스에서도 재검증)
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 비어 있습니다.");
        }

        // 서비스: 파일 검증 → S3 업로드 → tb_interview INSERT
        Interview saved = interviewService.createInterview(userId, jobApplied, file);

        // 응답 DTO 변환 (정적 팩토리 가정)
        InterviewResponseDto body = InterviewResponseDto.from(saved);

        // Location 헤더로 리소스 위치 제공
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/api/interview/" + saved.getId())
                .body(body);
    }
}