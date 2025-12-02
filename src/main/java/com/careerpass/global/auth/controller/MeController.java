package com.careerpass.global.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MeController {

    @GetMapping("/health")
    public String health() {
        return "UP";
    }

    /**
     * 🎭 시연용 /me
     *
     *  - 실제 로그인 여부, DB 상태 전혀 안 봄
     *  - 항상 같은 더미 유저 정보를 200으로 반환
     *  - 프론트에서는 "로그인된 유저"라고 생각하고 동작하게 됨
     */
    @GetMapping("/me")
    public Map<String, Object> me() {
        Map<String, Object> mock = new HashMap<>();

        // 🔑 프론트가 쓸만한 기본 필드들 – 키 이름은 자유롭게 더/빼도 됨
        mock.put("id", 1L);
        mock.put("email", "demo@careerpass.com");
        mock.put("major", "컴퓨터공학과");
        mock.put("job", "데이터베이스 개발자");

        // 학습 프로필 쪽에서 쓸 수 있는 통계값들 (대충 시연용)
        mock.put("totalInterviewCount", 0);
        mock.put("totalIntroductionCount", 0);

        // 필요하면 여기다 다른 필드도 추가 가능
        // mock.put("name", "데모 유저");
        // mock.put("grade", 3);

        return mock; // -> 항상 200 OK + JSON
    }
}