package com.careerpass.domain.feedback.controller;

import com.careerpass.domain.feedback.dto.FeedbackDtos.CreateRequest;
import com.careerpass.domain.feedback.dto.FeedbackDtos.Response;
import com.careerpass.domain.feedback.dto.FeedbackDtos.TitleResponse;
import com.careerpass.domain.feedback.dto.FeedbackDtos.UpdateTitleRequest;
import com.careerpass.domain.feedback.dto.InterviewAiDtos;
import com.careerpass.domain.feedback.dto.InterviewSessionDtos;
import com.careerpass.domain.feedback.service.FeedbackService;
import com.careerpass.domain.feedback.service.InterviewSessionService;
import com.careerpass.domain.user.entity.User;
import com.careerpass.domain.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.careerpass.domain.feedback.dto.IntroductionAiDtos.IntroFeedbackDispatch;
import com.careerpass.domain.feedback.dto.IntroductionAiDtos.IntroFeedbackRequest;
import com.careerpass.domain.feedback.dto.IntroductionAiDtos.IntroFeedbackResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final InterviewSessionService interviewSessionService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

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

    @Operation(summary = "내 피드백 삭제 (자기소개서/면접 공통)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable @Positive(message = "id는 양수여야 합니다.") Long id,
            Principal principal
    ) {
        Long userId = resolveUserId(principal);
        feedbackService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "피드백 제목 수정")
    @PatchMapping("/{id}/title")
    public ResponseEntity<TitleResponse> updateTitle(
            @PathVariable @Positive(message = "id는 양수여야 합니다.") Long id,
            @RequestBody @Valid UpdateTitleRequest req,
            Principal principal
    ) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(feedbackService.updateTitle(id, userId, req.title()));
    }

    // 자기소개서(id) 기준 리스트
    @Operation(summary = "자기소개서 피드백 리스트 조회")
    @GetMapping("/introduction/{introductionId}")
    public ResponseEntity<List<Response>> listByIntroduction(@PathVariable @Positive Long introductionId) {
        return ResponseEntity.ok(feedbackService.listByIntroduction(introductionId));
    }

    // 면접 결과지 상세 조회
    @Operation(summary = "면접 결과지 상세 조회")
    @GetMapping("/interview/{interviewId}")
    public ResponseEntity<InterviewSessionDtos.InterviewResultSheetDto> getInterviewResult(
            @PathVariable @Positive Long interviewId,
            Principal principal
    ) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(interviewSessionService.getResultSheet(userId, interviewId));
    }

    // 면접 결과지 삭제 (세션 단위)
    @Operation(summary = "면접 결과지 삭제")
    @DeleteMapping("/interview/{interviewId}")
    public ResponseEntity<Void> deleteInterviewSession(
            @PathVariable @Positive Long interviewId,
            Principal principal
    ) {
        Long userId = resolveUserId(principal);
        interviewSessionService.deleteSession(userId, interviewId);
        return ResponseEntity.noContent().build();
    }

    // 면접 결과지 제목 수정
    @Operation(summary = "면접 결과지 제목 수정")
    @PatchMapping("/interview/{interviewId}/title")
    public ResponseEntity<InterviewSessionDtos.TitleResponse> updateInterviewTitle(
            @PathVariable @Positive Long interviewId,
            @RequestBody @Valid InterviewSessionDtos.UpdateTitleRequest req,
            Principal principal
    ) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(interviewSessionService.updateTitle(userId, interviewId, req.title()));
    }

    // 면접 시작 (세션 생성)
    @Operation(summary = "면접 시작 (세션 생성)")
    @PostMapping("/interview/start")
    public ResponseEntity<InterviewSessionDtos.StartResponse> startInterview(
            @RequestBody @Valid InterviewSessionDtos.StartRequest req,
            Principal principal
    ) {
        Long userId = resolveUserId(principal);
        Long interviewId = interviewSessionService.createSession(userId, req.jobApplied()).getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(new InterviewSessionDtos.StartResponse(interviewId));
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

    // 면접 결과지 히스토리
    @Operation(summary = "내 면접 결과지 히스토리")
    @GetMapping("/interview/history")
    public ResponseEntity<List<InterviewSessionDtos.InterviewSessionSummaryDto>> listInterviewHistory(
            Principal principal
    ) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(interviewSessionService.listHistory(userId));
    }

    // 면접 결과지 확정
    @Operation(summary = "면접 결과지 확정")
    @PostMapping("/interview/finalize")
    public ResponseEntity<InterviewSessionDtos.InterviewResultSheetDto> finalizeInterview(
            @RequestBody @Valid InterviewSessionDtos.FinalizeRequest req,
            Principal principal
    ) {
        Long userId = resolveUserId(principal);
        InterviewSessionDtos.InterviewResultSheetDto result = interviewSessionService
                .finalizeSession(userId, req.interviewId(), req.totalDurationSec());
        return ResponseEntity.ok(result);
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
    @PostMapping(
            value = "/interview/ai",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = InterviewAiMultipartRequest.class),
                    // ...
                    encoding = {
                            // 🚨 여기가 핵심: meta 파트를 JSON으로 인코딩하도록 명시
                            @Encoding(name = "meta", contentType = "application/json"),
                            // file 파트는 바이너리 스트림
                            @Encoding(name = "file", contentType = "application/octet-stream"),
                            @Encoding(name = "interviewDuration", contentType = "text/plain")
                    }
            )
    )
    public ResponseEntity<InterviewAiDtos.AnswerAnalysisResultDto> processInterviewAnswer(
            Principal principal,
            // 프론트에서 전송된 JSON 형태의 메타데이터를 받습니다.
            @RequestPart(value = "meta", required = false) String metaJson,
            // 프론트에서 전송된 음성 파일 (.m4a, .mp3 등)을 받습니다.
            @Parameter(
                    description = "업로드할 음성 파일",
                    required = true
            )
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "interviewDuration", required = false) Long interviewDuration,
            HttpServletRequest request
    ) {
        log.info("[INTERVIEW_AI] contentType={}, duration={}, metaLen={}, fileType={}, fileSize={}",
                request.getContentType(),
                interviewDuration,
                metaJson == null ? null : metaJson.length(),
                file != null ? file.getContentType() : null,
                file != null ? file.getSize() : null);
        log.info("[STT] received audio: name={}, size={}, contentType={}",
                file != null ? file.getOriginalFilename() : null,
                file != null ? file.getSize() : null,
                file != null ? file.getContentType() : null);
        Long userId = resolveUserId(principal);
        InterviewAiDtos.SttRequestMetaDto meta = parseMeta(metaJson);
        validateMeta(meta, interviewDuration);
        InterviewAiDtos.SttRequestMetaDto fixedMeta = new InterviewAiDtos.SttRequestMetaDto(
                meta.interviewId(),
                meta.questionId(),
                meta.questionText(),
                meta.resumeContent(),
                meta.jobApplied(),
                interviewDuration
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

    private InterviewAiDtos.SttRequestMetaDto parseMeta(String metaJson) {
        if (metaJson == null || metaJson.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "meta는 필수입니다.");
        }
        try {
            return objectMapper.readValue(metaJson, InterviewAiDtos.SttRequestMetaDto.class);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "meta는 JSON 형식이어야 합니다.");
        }
    }

    private void validateMeta(InterviewAiDtos.SttRequestMetaDto meta, Long interviewDuration) {
        if (meta.interviewId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "interviewId는 필수입니다.");
        }
        if (meta.questionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "questionId는 필수입니다.");
        }
        if (meta.questionText() == null || meta.questionText().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "questionText는 필수입니다.");
        }
        if (meta.resumeContent() == null || meta.resumeContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "resumeContent는 필수입니다.");
        }
        if (meta.jobApplied() == null || meta.jobApplied().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "jobApplied는 필수입니다.");
        }
        if (interviewDuration == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "interviewDuration은 필수입니다.");
        }
    }

    private static class InterviewAiMultipartRequest {
        @Schema(description = "업로드할 음성 파일", type = "string", format = "binary")
        public MultipartFile file;

        @Schema(description = "STT 요청 메타 JSON 문자열", example = "{\"interviewId\":1,\"questionId\":1,\"questionText\":\"...\",\"resumeContent\":\"...\",\"jobApplied\":\"...\"}")
        public String meta;

        @Schema(description = "면접 시간(ms)", example = "120000")
        public Long interviewDuration;
    }
}
