package com.careerpass.domain.roadmap.service;

import com.careerpass.domain.roadmap.entity.Roadmap;
import com.careerpass.domain.roadmap.entity.RoadmapType;
import com.careerpass.domain.roadmap.exception.RoadmapNotFoundException;
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
     * - major만 받음
     * - grade, job으로는 필터하지 않음
     */
    public List<Roadmap> getMajorRoadmap(String major) {
        List<Roadmap> result =
                roadmapRepository.findByRoadmapTypeAndMajorOrderByGradeAscIdAsc(
                        RoadmapType.MAJOR,
                        major
                );

        // 예외 처리
        if (result.isEmpty()) {
            throw new RoadmapNotFoundException("해당 전공의 교과목 로드맵이 없습니다. major=" + major);
        }

        return result;
    }

    /**
     * 자격증(CERTIFICATION) 로드맵 조회 (직무별)
     */
    public List<Roadmap> getCertificationRoadmap(String major, String job) {
        List<Roadmap> result =
                roadmapRepository.findByRoadmapTypeAndMajorAndJobOrderByGradeAscIdAsc(
                        RoadmapType.CERTIFICATION,
                        major,
                        job
                );

        // 예외 처리
        if (result.isEmpty()) {
            throw new RoadmapNotFoundException(
                    "해당 전공/직무의 자격증 로드맵이 없습니다. major=" + major + ", job=" + job
            );
        }

        return result;
    }
}