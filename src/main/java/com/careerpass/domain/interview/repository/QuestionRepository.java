package com.careerpass.domain.interview.repository;

import com.careerpass.domain.interview.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByIntroductionIdOrderByOrderIndexAsc(Long introId);

    boolean existsByIntroductionId(Long introId);

    void deleteByIntroductionId(Long introId);
}