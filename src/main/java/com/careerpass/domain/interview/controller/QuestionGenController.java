package com.careerpass.domain.interview.controller;

import com.careerpass.domain.interview.dto.GenerateQuestionsRequest;
import com.careerpass.domain.interview.dto.GenerateQuestionsResponse;
import com.careerpass.domain.interview.service.QuestionGenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 🧠 면접 질문 생성 컨트롤러
 * - Flask(question_ai.py)에 coverLetter 전달
 * - major/jobTitle은 내부에서 userId 기반 조회
 */
@RestController
@RequestMapping("/api/interview/question-gen")
@RequiredArgsConstructor
@Slf4j
@Validated
public class QuestionGenController {

    private final QuestionGenService questionGenService;

    /**
     * ▶ POST /api/interview/question-gen
     * Body: { userId, coverLetter }
     */
    @PostMapping
    public Mono<ResponseEntity<GenerateQuestionsResponse>> generate(
            @RequestBody GenerateQuestionsRequest req
    ) {
        log.info("📨 질문 생성 요청 도착: userId={}, coverLetter length={}",
                req.userId(),
                req.coverLetter() != null ? req.coverLetter().length() : 0
        );

        return questionGenService.generate(req)
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("❌ 질문 생성 실패", e));
    }
}