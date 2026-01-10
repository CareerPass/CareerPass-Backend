package com.careerpass.domain.feedback.repository;

import com.careerpass.domain.feedback.entity.InterviewAnswer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {

    List<InterviewAnswer> findBySessionIdOrderByIdAsc(Long sessionId);

    void deleteBySessionId(Long sessionId);
}
