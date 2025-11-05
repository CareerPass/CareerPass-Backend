package com.careerpass.domain.introduction.controller;

import com.careerpass.domain.introduction.dto.IntroductionDtos.CreateRequest;
import com.careerpass.domain.introduction.dto.IntroductionDtos.Response;
import com.careerpass.domain.introduction.service.IntroductionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/introductions")
@Validated
public class IntroductionController {

    private final IntroductionService introductionService;

    // 자기소개서 저장
    @PostMapping
    public ResponseEntity<Response> create(@RequestBody @Valid CreateRequest req) {
        Response created = introductionService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<Response> get(@PathVariable @Positive(message = "id는 양수여야 합니다.") Long id) {
        return ResponseEntity.ok(introductionService.get(id));
    }

    // 사용자별 목록 조회
    @GetMapping
    public ResponseEntity<List<Response>> listByUser(@RequestParam @NotNull(message = "userId는 필수입니다.") Long userId) {
        return ResponseEntity.ok(introductionService.listByUser(userId));
    }
}