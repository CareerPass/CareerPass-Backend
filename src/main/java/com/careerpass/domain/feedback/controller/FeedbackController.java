package com.careerpass.domain.feedback.controller;

import com.careerpass.domain.feedback.dto.FeedbackDtos.CreateRequest;
import com.careerpass.domain.feedback.dto.FeedbackDtos.Response;
    import com.careerpass.domain.feedback.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
    import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

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
}