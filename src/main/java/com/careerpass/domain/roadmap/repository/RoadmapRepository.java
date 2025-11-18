package com.careerpass.domain.roadmap.repository;

import com.careerpass.domain.roadmap.entity.Roadmap;
import com.careerpass.domain.roadmap.entity.RoadmapType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 로드맵 Repository
 * - 전공(major) + 직무(job) + 로드맵 타입 기준 조회
 */
public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    List<Roadmap> findByRoadmapTypeAndMajorAndJob(
            RoadmapType type,
            String major,
            String job
    );
}