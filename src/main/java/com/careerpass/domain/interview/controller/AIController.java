package com.careerpass.domain.interview.controller;

import com.careerpass.domain.interview.dto.VoiceAnalyzeResponse;
import com.careerpass.domain.interview.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interview/voice")
public class AIController {

    private final AIService aiService;

    @PostMapping(value = "/analyze", consumes = {"multipart/form-data"})
    public Mono<ResponseEntity<VoiceAnalyzeResponse>> analyze(@RequestPart("file") MultipartFile file) {
        return aiService.analyzeVoice(file)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }
}