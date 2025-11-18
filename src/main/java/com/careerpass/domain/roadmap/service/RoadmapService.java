package com.careerpass.domain.roadmap.service;

import com.careerpass.domain.roadmap.entity.Roadmap;
import com.careerpass.domain.roadmap.entity.RoadmapType;
import com.careerpass.domain.roadmap.repository.RoadmapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 로드맵 Service
 * - 교과목(MAJOR)
 * - 자격증(CERTIFICATION)
 */
@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;

    /**
     * 교과목(MAJOR) 로드맵 조회
     */
    public List<Roadmap> getMajorRoadmap(String major, String job) {
        return roadmapRepository.findByRoadmapTypeAndMajorAndJob(
                RoadmapType.MAJOR, major, job
        );
    }

    /**
     * 자격증(CERTIFICATION) 로드맵 조회
     */
    public List<Roadmap> getCertificationRoadmap(String major, String job) {
        return roadmapRepository.findByRoadmapTypeAndMajorAndJob(
                RoadmapType.CERTIFICATION, major, job
        );
    }
}