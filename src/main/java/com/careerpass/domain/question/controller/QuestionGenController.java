package com.careerpass.domain.question.controller;

import com.careerpass.domain.question.dto.GenerateQuestionsRequest;
import com.careerpass.domain.question.dto.GenerateQuestionsResponse;
import com.careerpass.domain.question.service.QuestionGenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interview/question-gen")
public class QuestionGenController {

    private final QuestionGenService questionGenService;

    /**
     * 프론트 → (자기소개서 기반) 면접 질문 생성
     * Body 예시:
     * {
     *   "major": "컴퓨터공학과",
     *   "jobTitle": "백엔드 개발자"
     * }
     */
    @PostMapping
    public Mono<ResponseEntity<GenerateQuestionsResponse>> generate(
            @RequestBody GenerateQuestionsRequest body
    ) {
        return questionGenService.generate(body.major(), body.jobTitle())
                .map(ResponseEntity::ok);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }
}