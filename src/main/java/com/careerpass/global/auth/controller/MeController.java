package com.careerpass.global.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MeController {

    @GetMapping("/health")
    public String health() {
        return "UP";
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String email = String.valueOf(authentication.getPrincipal());

        Map<String, Object> res = new HashMap<>();
        res.put("email", email);

        // ✅ DB 연동 전: 비워둠
        res.put("name", null);
        res.put("major", null);
        res.put("job", null);
        res.put("totalInterviewCount", 0);
        res.put("totalIntroductionCount", 0);

        return res;
    }
}