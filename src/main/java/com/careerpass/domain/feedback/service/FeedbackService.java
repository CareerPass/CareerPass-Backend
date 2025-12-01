package com.careerpass.domain.feedback.service;

import com.careerpass.domain.feedback.dto.FeedbackDtos.CreateRequest;
import com.careerpass.domain.feedback.dto.FeedbackDtos.Response;
import com.careerpass.domain.feedback.dto.InterviewAiDtos;
import com.careerpass.domain.feedback.entity.Feedback;
import com.careerpass.domain.feedback.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.careerpass.domain.feedback.dto.IntroductionAiDtos.IntroFeedbackRequest;
import com.careerpass.domain.feedback.dto.IntroductionAiDtos.IntroFeedbackResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    // 🔹 파이썬 Resume AI 서버 WebClient
    private final WebClient resumeAiClient = WebClient.builder()
            .baseUrl("http://localhost:8000") // 지금 uvicorn 띄운 주소
            .build();

    // 피드백 생성
    @Transactional
    public Response create(CreateRequest req) {
        Feedback feedback = Feedback.builder()
                .title(req.title())
                .feedbackType(req.feedbackType())
                .totalScore(req.totalScore())
                .feedbackText(req.feedbackText())
                .sectionFeedback(req.sectionFeedback())
                .introductionId(req.introductionId())
                .interviewId(req.interviewId())
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        return toDto(saved);
    }

    // 단일 피드백 조회
    @Transactional(readOnly = true)
    public Response get(Long id) {
        Feedback f = feedbackRepository.findById(id)
                .orElseThrow(() -> new com.careerpass.domain.feedback.exception.FeedbackNotFoundException(id));
        return toDto(f);
    }

    // 자기소개서 기반 피드백 리스트 조회
    @Transactional(readOnly = true)
    public List<Response> listByIntroduction(Long introductionId) {
        return feedbackRepository.findByIntroductionIdOrderByIdDesc(introductionId)
                .stream().map(this::toDto).toList();
    }

    // 면접 기반 피드백 리스트 조회
    @Transactional(readOnly = true)
    public List<Response> listByInterview(Long interviewId) {
        return feedbackRepository.findByInterviewIdOrderByIdDesc(interviewId)
                .stream().map(this::toDto).toList();
    }

    /**
     * 자소서 AI 피드백 생성 (파이썬 FastAPI 호출)
     */
    @Transactional(readOnly = true)
    public IntroFeedbackResponse createIntroAiFeedback(IntroFeedbackRequest req) {

        try {
            return resumeAiClient.post()
                    .uri("/resume/resume/feedback")   // 🔴 파이썬 @resume_router.post("/resume/feedback")
                    .bodyValue(req)           // { "userId": .., "resumeContent": "..." }
                    .retrieve()
                    .bodyToMono(IntroFeedbackResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            // 파이썬 쪽 4xx/5xx
            throw new RuntimeException("Python Resume AI 서버 호출 실패: " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            // 네트워크 등 기타 오류
            throw new RuntimeException("Python Resume AI 서버 연결 중 오류 발생", ex);
        }
    }

    /**
     * 면접 답변 AI 분석 (파이썬 FastAPI 호출)
     */
    @Transactional(readOnly = true)
    public InterviewAiDtos.AnswerAnalysisResultDto analyzeInterviewAnswer(InterviewAiDtos.AnswerDispatchDto dispatch) {

        try {
            return resumeAiClient.post()
                    .uri("/analysis/interview/run") // 🔴 파이썬 interview_router 엔드포인트
                    .bodyValue(dispatch)
                    .retrieve()
                    .bodyToMono(InterviewAiDtos.AnswerAnalysisResultDto.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RuntimeException("Python Interview AI 서버 호출 실패: " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            throw new RuntimeException("Python Interview AI 서버 연결 중 오류 발생", ex);
        }
    }



    private Response toDto(Feedback f) {
        return new Response(
                f.getId(),
                f.getTitle(),
                f.getFeedbackType(),
                f.getTotalScore(),
                f.getFeedbackText(),
                f.getSectionFeedback(),
                f.getIntroductionId(),
                f.getInterviewId()
        );
    }
}