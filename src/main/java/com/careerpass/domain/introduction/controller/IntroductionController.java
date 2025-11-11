package com.careerpass.domain.introduction.controller;

import com.careerpass.domain.introduction.dto.IntroductionDtos.CreateRequest;
import com.careerpass.domain.introduction.dto.IntroductionDtos.Response;
import com.careerpass.domain.introduction.service.IntroductionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/introductions", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Slf4j
public class IntroductionController {

    private final IntroductionService introductionService;

    /**
     * 자기소개서 저장
     * - req.submissionTime 이 null 일 수 있음 → Service에서 now()로 대체 권장
     * - 생성 성공 시 Location 헤더에 /api/introductions/{id} 반환
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> create(@RequestBody @Valid CreateRequest req) {
        log.debug("POST /api/introductions - userId={}, jobAppliedLen={}, introLen={}",
                req.userId(),
                req.jobApplied() != null ? req.jobApplied().length() : null,
                req.introText() != null ? req.introText().length() : null);

        Response created = introductionService.create(req);

        // Location: 현재 요청 경로 + /{id}
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity
                .created(location) // 201 + Location 헤더
                .body(created);
    }

    /**
     * 단건 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<Response> get(@PathVariable @Positive(message = "id는 양수여야 합니다.") Long id) {
        log.debug("GET /api/introductions/{}", id);
        return ResponseEntity.ok(introductionService.get(id));
    }

    /**
     * 사용자별 목록 조회
     * - 추후 페이징 필요하면 page, size 파라미터 추가 권장
     */
    @GetMapping
    public ResponseEntity<List<Response>> listByUser(
            @RequestParam
            @NotNull(message = "userId는 필수입니다.")
            @Positive(message = "userId는 1 이상이어야 합니다.")
            Long userId
    ) {
        log.debug("GET /api/introductions?userId={}", userId);
        return ResponseEntity.ok(introductionService.listByUser(userId));
    }
}