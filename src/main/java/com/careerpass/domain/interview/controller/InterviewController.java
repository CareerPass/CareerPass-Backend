package com.careerpass.domain.interview.controller;


import com.careerpass.domain.interview.entity.Interview;
import com.careerpass.domain.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /**
     * 🎤 음성 파일 업로드 및 인터뷰 등록 API
     * [POST] /interview/audio
     *
     * - 프론트에서 질문별 음성 파일 업로드 시 사용
     * - Whisper로 텍스트 변환 후 AI 분석 로직으로 연결 가능
     *
     * Params:
     *  - file: MultipartFile (녹음된 음성 파일)
     *  - userId: Long (사용자 식별자)
     *  - jobApplied: String (지원 직무명)
     *
     * Returns:
     *  - Interview Entity (DB에 저장된 인터뷰 정보)
     *  - HTTP 201: 생성 성공
     *  - HTTP 400: 잘못된 요청
     *  - HTTP 500: 서버 내부 오류
     */
    @PostMapping("/audio")
    public ResponseEntity<Interview> registerInterview(
            @RequestPart("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("jobApplied") String jobApplied
    ) {
        try {
            // ✅ 파일 검증
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // ✅ 서비스 호출 (음성 파일 저장 및 인터뷰 등록)
            Interview savedInterview = interviewService.createInterview(userId, jobApplied, file);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedInterview);

        } catch (IOException e) {
            // ❌ 파일 저장 또는 Whisper 변환 시 I/O 예외
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            // ❌ 기타 예외 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}