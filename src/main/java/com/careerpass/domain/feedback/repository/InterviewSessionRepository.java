package com.careerpass.domain.feedback.repository;

import com.careerpass.domain.feedback.entity.InterviewSession;
import com.careerpass.domain.feedback.entity.SessionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    Optional<InterviewSession> findByIdAndUserId(Long id, Long userId);

    List<InterviewSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<InterviewSession> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, SessionStatus status);

    long countByUserIdAndStatus(Long userId, SessionStatus status);

    long countByUserId(Long userId);
}
