package com.careerpass.domain.introduction.repository;

import com.careerpass.domain.introduction.entity.Introduction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IntroductionRepository extends JpaRepository<Introduction, Long> {
    List<Introduction> findByUserIdOrderBySubmissionTimeDesc(Long userId);
}