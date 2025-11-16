package com.careerpass.domain.introduction.service;

import com.careerpass.domain.introduction.dto.IntroductionDtos.CreateRequest;
import com.careerpass.domain.introduction.dto.IntroductionDtos.Response;
import com.careerpass.domain.introduction.entity.Introduction;
import com.careerpass.domain.introduction.exception.IntroductionNotFoundException;
import com.careerpass.domain.introduction.repository.IntroductionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본은 조회 트랜잭션
public class IntroductionService {

    private final IntroductionRepository introductionRepository;

    /**
     * 자기소개서 생성
     * - req.submissionTime == null 이면 서버 시간으로 대체
     * - 입력 문자열은 trim/strip 해서 공백만 있는 입력을 방지
     */
    @Transactional // 쓰기 트랜잭션
    public Response create(CreateRequest req) {
        // 컨트롤러에서 @Valid로 1차 검증하지만, 안전하게 한 번 더 정규화
        String jobApplied = req.jobApplied() != null ? req.jobApplied().trim() : null;
        String introText  = req.introText()  != null ? req.introText().strip() : null;

        // 빈 문자열 방지(validator가 걸러주지만, 실무에서 방어코드 추천)
        if (jobApplied == null || jobApplied.isEmpty()) {
            throw new IllegalArgumentException("지원 직무(jobApplied)가 비어 있습니다.");
        }
        if (introText == null || introText.isEmpty()) {
            throw new IllegalArgumentException("자기소개 내용(introText)이 비어 있습니다.");
        }

        // 제출 시간이 없으면 현재 시간
        LocalDateTime submittedAt = (req.submissionTime() != null)
                ? req.submissionTime()
                : LocalDateTime.now();

        Introduction entity = Introduction.builder()
                .userId(req.userId())
                .jobApplied(jobApplied)
                .introText(introText)
                .submissionTime(submittedAt)
                .build();

        Introduction saved = introductionRepository.save(entity);
        return toDto(saved);
    }

    /**
     * 자기소개서 단건 조회
     */
    public Response get(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("유효하지 않은 ID입니다: " + id);
        }
        Introduction found = introductionRepository.findById(id)
                .orElseThrow(() -> new IntroductionNotFoundException(id));
        return toDto(found);
    }

    /**
     * 특정 사용자의 자기소개서 목록 조회 (최신 제출일 순)
     */
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
     * Entity → DTO 변환 (NPE 방지)
     */
    private Response toDto(Introduction i) {
        if (i == null) {
            throw new IllegalStateException("Introduction 엔티티가 null 입니다.");
        }
        return new Response(
                i.getId(),
                i.getUserId(),
                i.getJobApplied(),
                i.getIntroText(),
                i.getSubmissionTime()
        );
    }
}