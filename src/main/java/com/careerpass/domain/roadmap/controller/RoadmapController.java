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
     * 예: /api/roadmap/major?major=컴퓨터공학과&job=백엔드개발자
     */
    @GetMapping("/major")
    public List<Roadmap> getMajorRoadmap(
            @RequestParam String major,
            @RequestParam String job
    ) {
        return roadmapService.getMajorRoadmap(major, job);
    }

    /**
     * 자격증 로드맵 (CERTIFICATION)
     * 예: /api/roadmap/cert?major=컴퓨터공학과&job=백엔드개발자
     */
    @GetMapping("/cert")
    public List<Roadmap> getCertificationRoadmap(
            @RequestParam String major,
            @RequestParam String job
    ) {
        return roadmapService.getCertificationRoadmap(major, job);
    }
}