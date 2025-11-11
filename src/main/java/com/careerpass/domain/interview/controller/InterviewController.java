package com.careerpass.domain.interview.controller;

import com.careerpass.domain.interview.entity.Interview;
import com.careerpass.domain.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.IOException;

@RestController
@RequestMapping("/interview")
@RequiredArgsConstructor
@Slf4j
@Validated
public class InterviewController {

    private final InterviewService interviewService;

    /**
     * 🎤 음성 파일 업로드 및 인터뷰 등록 API
     * [POST] /interview/audio
     *
     * Params:
     *  - file: MultipartFile (녹음된 음성 파일)
     *  - userId: Long (사용자 식별자)
     *  - jobApplied: String (지원 직무명)
     *
     * Returns:
     *  - 201 Created + 저장된 Interview
     *  - 400 Bad Request (유효성 실패)
     *  - 500 Internal Server Error (업로드/IO 실패)
     */
    @PostMapping(
            value = "/audio",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Interview> registerInterview(
            @RequestPart("file") MultipartFile file,
            @RequestParam("userId") @NotNull @Positive Long userId,
            @RequestParam("jobApplied") @NotBlank String jobApplied
    ) {
        // 1) 기본 검증
        if (file == null || file.isEmpty()) {
            log.warn("Bad request: empty file (userId={}, jobApplied={})", userId, jobApplied);
            return ResponseEntity.badRequest().build();
        }

        try {
            // 2) 서비스 호출 (음성 파일 저장 및 인터뷰 등록)
            Interview savedInterview = interviewService.createInterview(userId, jobApplied, file);

            // 3) Location 헤더 제공 (리소스 추적 편의)
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header("Location", "/interview/" + savedInterview.getId())
                    .body(savedInterview);

        } catch (IOException e) {
            log.error("IO error during interview upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } catch (IllegalArgumentException e) {
            log.warn("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Unexpected error during interview upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}