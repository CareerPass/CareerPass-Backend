package com.careerpass.domain.interview.controller;


import com.careerpass.domain.interview.entity.Interview;
import com.careerpass.domain.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/audio")
    public ResponseEntity<Interview> registerInterview(
            @RequestPart("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("jobApplied")String jobApplied
            ) {
        try {
            if(file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Interview savedInterview = interviewService.createInterview(userId, jobApplied, file);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedInterview);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
