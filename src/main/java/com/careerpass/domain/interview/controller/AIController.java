package com.careerpass.domain.interview.controller;

import com.careerpass.domain.interview.dto.VoiceAnalyzeResponse;
import com.careerpass.domain.interview.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interview/voice")
public class AIController {

    private final AIService aiService;

    @PostMapping(value = "/analyze", consumes = {"multipart/form-data"})
    public ResponseEntity<VoiceAnalyzeResponse> analyze(@RequestPart("file") MultipartFile file) {
        VoiceAnalyzeResponse res = aiService.analyzeVoice(file);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }
}