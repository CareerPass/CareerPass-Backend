package com.careerpass.domain.introduction.repository;

import com.careerpass.domain.introduction.entity.Introduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 자기소개서 Repository
 * - 사용자별 자기소개서 조회 및 최신순 정렬
 */
@Repository
public interface IntroductionRepository extends JpaRepository<Introduction, Long> {

    /**
     * 특정 사용자의 자기소개서 목록을 제출일 내림차순으로 조회
     */
    List<Introduction> findByUserIdOrderBySubmissionTimeDesc(Long userId);

    /**
     * (선택) 특정 사용자의 최신 자기소개서 1건만 가져오기
     * - 면접 질문 생성 시 최근 자기소개서 기반으로 사용할 때 유용
     */
    default Introduction findLatestByUserId(List<Introduction> introductions) {
        return (introductions == null || introductions.isEmpty()) ? null : introductions.get(0);
    }
}