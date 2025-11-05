package com.careerpass.domain.feedback.controller;

import com.careerpass.domain.feedback.dto.FeedbackDtos.CreateRequest;
import com.careerpass.domain.feedback.dto.FeedbackDtos.Response;
import com.careerpass.domain.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;

    // 피드백 생성 (AI 연동 전: 저장만)
    @PostMapping
    public ResponseEntity<Response> create(@RequestBody @Valid CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackService.create(req));
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<Response> get(@PathVariable Long id) {
        return ResponseEntity.ok(feedbackService.get(id));
    }

    // 자기소개서(id) 기준 리스트
    @GetMapping("/introduction/{introductionId}")
    public ResponseEntity<List<Response>> listByIntroduction(@PathVariable Long introductionId) {
        return ResponseEntity.ok(feedbackService.listByIntroduction(introductionId));
    }

    // 면접(id) 기준 리스트
    @GetMapping("/interview/{interviewId}")
    public ResponseEntity<List<Response>> listByInterview(@PathVariable Long interviewId) {
        return ResponseEntity.ok(feedbackService.listByInterview(interviewId));
    }
}