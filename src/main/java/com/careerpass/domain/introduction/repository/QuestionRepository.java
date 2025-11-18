package com.careerpass.domain.introduction.repository;

import com.careerpass.domain.introduction.entity.Question;
import com.careerpass.domain.introduction.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 자소서 기반 질문 레포지토리
 * - 특정 자기소개서(Introduction)에서 생성된 질문 리스트 조회
 * - 자소서 수정/재생성 시 기존 질문 삭제 등
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * 특정 자기소개서에 대한 질문들을 순서대로 조회
     */
    List<Question> findByIntroductionIdOrderByOrderIndexAsc(Long introductionId);

    /**
     * 특정 자기소개서에 대한 질문 전체 삭제
     * (자소서를 수정하거나, 질문을 재생성할 때 사용할 수 있음)
     */
    void deleteByIntroductionId(Long introductionId);

    /**
     * 해당 자기소개서에 이미 질문이 저장되어 있는지 체크
     */
    boolean existsByIntroductionId(Long introductionId);
}