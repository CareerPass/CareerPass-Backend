package com.careerpass.domain.feedback.controller;

import com.careerpass.domain.feedback.dto.FeedbackDtos.CreateRequest;
import com.careerpass.domain.feedback.dto.FeedbackDtos.Response;
import com.careerpass.domain.feedback.dto.InterviewAiDtos;
import com.careerpass.domain.feedback.service.FeedbackService;
import com.careerpass.domain.user.entity.User;
import com.careerpass.domain.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.careerpass.domain.feedback.dto.IntroductionAiDtos.IntroFeedbackDispatch;
import com.careerpass.domain.feedback.dto.IntroductionAiDtos.IntroFeedbackRequest;
import com.careerpass.domain.feedback.dto.IntroductionAiDtos.IntroFeedbackResponse;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.Principal;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserRepository userRepository;

    // 피드백 생성 (AI 연동 전: 저장만)
    @Operation(summary = "피드백 생성")
    @PostMapping
    public ResponseEntity<Response> create(@RequestBody @Valid CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackService.create(req));
    }

    // 단건 조회
    @Operation(summary = "피드백 단건 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<Response> get(@PathVariable @Positive(message = "id는 양수여야 합니다.") Long id) {
        return ResponseEntity.ok(feedbackService.get(id));
    }

    // 자기소개서(id) 기준 리스트
    @Operation(summary = "자기소개서 피드백 리스트 조회")
    @GetMapping("/introduction/{introductionId}")
    public ResponseEntity<List<Response>> listByIntroduction(@PathVariable @Positive Long introductionId) {
        return ResponseEntity.ok(feedbackService.listByIntroduction(introductionId));
    }

    // 면접(id) 기준 리스트
    @Operation(summary = "면접 피드백 리스트 조회")
    @GetMapping("/interview/{interviewId}")
    public ResponseEntity<List<Response>> listByInterview(@PathVariable @Positive Long interviewId) {
        return ResponseEntity.ok(feedbackService.listByInterview(interviewId));
    }

    // 본인 자기소개서 피드백 요약
    @Operation(summary = "내 자기소개서 피드백 요약 리스트")
    @GetMapping("/me/introduction")
    public ResponseEntity<List<com.careerpass.domain.feedback.dto.FeedbackDtos.SummaryResponse>> listMyIntroduction(
            Principal principal
    ) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(feedbackService.listMyIntroductionSummaries(userId));
    }

    // 본인 면접 피드백 요약
    @Operation(summary = "내 면접 피드백 요약 리스트")
    @GetMapping("/me/interview")
    public ResponseEntity<List<com.careerpass.domain.feedback.dto.FeedbackDtos.SummaryResponse>> listMyInterview(
            Principal principal
    ) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(feedbackService.listMyInterviewSummaries(userId));
    }


    // ===================== 🔹 자소서 AI 피드백 (파이썬 호출) =====================
    @Operation(summary = "자기소개서 AI 피드백 생성 (Python AI)")
    @PostMapping("/introduction/ai")
    public Mono<ResponseEntity<Response>> createIntroAiFeedback(
            Principal principal,
            @RequestParam(value = "introductionId", required = false) Long introductionId,
            @RequestBody @Valid IntroFeedbackRequest req
    ) {
        Long userId = resolveUserId(principal);

        IntroFeedbackDispatch dispatch = new IntroFeedbackDispatch(userId, req.resumeContent());

        return feedbackService.createIntroAiFeedback(dispatch)
                .flatMap(aiRes ->
                        Mono.fromCallable(() -> feedbackService.saveIntroductionFeedback(userId, introductionId, aiRes))
                                .subscribeOn(Schedulers.boundedElastic())
                )
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    // ===================== 🔹 면접 AI 답변 분석 (Python AI) =====================
    @Operation(summary = "면접 음성 답변 처리 (STT -> AI 분석 -> 저장)")
    @PostMapping(value = "/interview/ai", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    // ...
                    encoding = {
                            // 🚨 여기가 핵심: meta 파트를 JSON으로 인코딩하도록 명시
                            @Encoding(name = "meta", contentType = "application/json"),
                            // file 파트는 바이너리 스트림
                            @Encoding(name = "file", contentType = "application/octet-stream")
                    }
            )
    )
    public ResponseEntity<InterviewAiDtos.AnswerAnalysisResultDto> processInterviewAnswer(
            Principal principal,
            // 프론트에서 전송된 JSON 형태의 메타데이터를 받습니다.
            @RequestPart("meta") @Valid InterviewAiDtos.SttRequestMetaDto meta,
            // 프론트에서 전송된 음성 파일 (.m4a, .mp3 등)을 받습니다.
            @Parameter(
                    description = "업로드할 음성 파일",
                    required = true
            )
            @RequestPart("file") MultipartFile file
    ) {
        Long userId = resolveUserId(principal);
        InterviewAiDtos.SttRequestMetaDto fixedMeta = new InterviewAiDtos.SttRequestMetaDto(
                meta.interviewId(),
                meta.questionId(),
                meta.questionText(),
                meta.resumeContent(),
                meta.jobApplied()
        );

        InterviewAiDtos.AnswerAnalysisResultDto result = feedbackService.processAnswerAudio(userId, fixedMeta, file);
        return ResponseEntity.ok(result);
    }

    private Long resolveUserId(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found by email"));
        return user.getId();
    }
}
