package com.careerpass.domain.interview.repository;

import com.careerpass.domain.interview.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewJpaRepository extends JpaRepository<Interview, Long> {
}
