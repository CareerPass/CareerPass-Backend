package com.careerpass.domain.introduction.service;

import com.careerpass.domain.introduction.dto.IntroductionDtos.CreateRequest;
import com.careerpass.domain.introduction.dto.IntroductionDtos.Response;
import com.careerpass.domain.introduction.entity.Introduction;
import com.careerpass.domain.introduction.repository.IntroductionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IntroductionService {

    private final IntroductionRepository introductionRepository;

    @Transactional
    public Response create(CreateRequest req) {
        LocalDateTime submittedAt = (req.submissionTime() != null)
                ? req.submissionTime()
                : LocalDateTime.now();

        Introduction entity = Introduction.builder()
                .userId(req.userId())
                .jobApplied(req.jobApplied())
                .introText(req.introText())
                .submissionTime(submittedAt)
                .build();

        Introduction saved = introductionRepository.save(entity);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public Response get(Long id) {
        Introduction found = introductionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Introduction not found: " + id));
        return toDto(found);
    }

    @Transactional(readOnly = true)
    public List<Response> listByUser(Long userId) {
        return introductionRepository.findByUserIdOrderBySubmissionTimeDesc(userId)
                .stream().map(this::toDto).toList();
    }

    private Response toDto(Introduction i) {
        return new Response(i.getId(), i.getUserId(), i.getJobApplied(), i.getIntroText(), i.getSubmissionTime());
    }
}