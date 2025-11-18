package com.careerpass.domain.introduction.controller;

import com.careerpass.domain.introduction.dto.SaveIntroductionLearningHistoryRequest;
import com.careerpass.domain.introduction.entity.Introduction;
import com.careerpass.domain.introduction.entity.IntroductionLearningHistory;
import com.careerpass.domain.introduction.repository.IntroductionRepository;
import com.careerpass.domain.introduction.service.IntroductionLearningHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * 📘 자소서 학습 기록 관련 API 컨트롤러
 *
 * - 자소서 기반으로 질문 학습을 진행했을 때
 *   해당 학습 이력을 DB에 저장하는 역할
 */
@RestController
@RequestMapping("/api/introduction-learning")
@RequiredArgsConstructor
public class IntroductionLearningHistoryController {

    private final IntroductionLearningHistoryService historyService;
    private final IntroductionRepository introductionRepository;

    /**
     * 🔹 자소서 학습 기록 저장 API
     *
     * [POST] /api/introduction-learning
     *
     * RequestBody:
     * {
     *   "userId": 1,
     *   "introductionId": 10,
     *   "questionCount": 5
     * }
     */
    @PostMapping
    public ResponseEntity<IntroductionLearningHistory> saveHistory(
            @RequestBody SaveIntroductionLearningHistoryRequest request
    ) {
        // 1) 자소서 존재 여부 확인
        Introduction introduction = introductionRepository.findById(request.introductionId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "존재하지 않는 자기소개서 ID 입니다: " + request.introductionId())
                );

        // 2) 서비스 호출하여 학습 기록 저장
        IntroductionLearningHistory saved = historyService.saveHistory(
                request.userId(),
                introduction,
                request.questionCount()
        );

        // 3) 저장된 엔티티 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}