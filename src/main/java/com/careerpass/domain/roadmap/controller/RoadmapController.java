package com.careerpass.domain.roadmap.controller;

import com.careerpass.domain.roadmap.entity.Roadmap;
import com.careerpass.domain.roadmap.service.RoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 로드맵 Controller
 * - 교과목 로드맵 API (MAJOR)
 * - 자격증 로드맵 API (CERTIFICATION)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roadmap")
public class RoadmapController {

    private final RoadmapService roadmapService;

    /**
     * 교과목 로드맵 (MAJOR)
     * 예: /api/roadmap/major?major=컴퓨터공학과
     * -> 응답에 grade=1~4가 다 섞여 있고,
     *    프론트가 grade 기준으로 나눠서 렌더링
     */
    @GetMapping("/major")
    public List<Roadmap> getMajorRoadmap(
            @RequestParam String major
    ) {
        return roadmapService.getMajorRoadmap(major);
    }

    /**
     * 자격증 로드맵 (CERTIFICATION)
     * 예: /api/roadmap/cert?major=컴퓨터공학과&job=데이터베이스 개발자
     */
    @GetMapping("/cert")
    public List<Roadmap> getCertificationRoadmap(
            @RequestParam String major,
            @RequestParam String job
    ) {
        return roadmapService.getCertificationRoadmap(major, job);
    }
}