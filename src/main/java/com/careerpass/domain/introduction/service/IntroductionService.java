package com.careerpass.domain.introduction.service;

import com.careerpass.domain.introduction.dto.IntroductionDtos.CreateRequest;
import com.careerpass.domain.introduction.dto.IntroductionDtos.Response;
import com.careerpass.domain.introduction.entity.Introduction;
import com.careerpass.domain.introduction.repository.IntroductionRepository;
import com.careerpass.domain.introduction.exception.IntroductionNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IntroductionService {

    private final IntroductionRepository introductionRepository;

    /**
     * 자기소개서 생성
     */
    @Transactional
    public Response create(@Valid CreateRequest req) {
        // 제출 시간이 없으면 현재 시간으로 대체
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

    /**
     * 자기소개서 단건 조회
     */
    @Transactional(readOnly = true)
    public Response get(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("유효하지 않은 ID입니다: " + id);
        }

        Introduction found = introductionRepository.findById(id)
                .orElseThrow(() -> new IntroductionNotFoundException(id));

        return toDto(found);
    }

    /**
     * 특정 사용자의 자기소개서 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Response> listByUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다: " + userId);
        }

        return introductionRepository.findByUserIdOrderBySubmissionTimeDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Entity → DTO 변환
     */
    private Response toDto(Introduction i) {
        return new Response(
                i.getId(),
                i.getUserId(),
                i.getJobApplied(),
                i.getIntroText(),
                i.getSubmissionTime()
        );
    }
}