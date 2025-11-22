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

    // 교과목용 (전공 기준으로만)
    List<Roadmap> findByRoadmapTypeAndMajorOrderByGradeAscIdAsc(
            RoadmapType roadmapType,
            String major
    );

    // 자격증용 (전공 + 직무 기준으로)
    List<Roadmap> findByRoadmapTypeAndMajorAndJobOrderByGradeAscIdAsc(
            RoadmapType roadmapType,
            String major,
            String job
    );

}