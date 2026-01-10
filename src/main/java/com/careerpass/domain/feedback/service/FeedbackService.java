package com.careerpass.domain.feedback.service;

import com.careerpass.domain.feedback.dto.FeedbackDtos.CreateRequest;
import com.careerpass.domain.feedback.dto.FeedbackDtos.Response;
import com.careerpass.domain.feedback.dto.FeedbackDtos.TitleResponse;
import com.careerpass.domain.feedback.dto.InterviewAiDtos;
import com.careerpass.domain.feedback.dto.IntroductionAiDtos.IntroFeedbackDispatch;
import com.careerpass.domain.feedback.dto.IntroductionAiDtos.IntroFeedbackRequest;
import com.careerpass.domain.feedback.dto.IntroductionAiDtos.IntroFeedbackResponse;
import com.careerpass.domain.feedback.entity.Feedback;
import com.careerpass.domain.feedback.entity.FeedbackType;
import com.careerpass.domain.feedback.entity.InterviewSession;
import com.careerpass.domain.feedback.entity.SessionStatus;
import com.careerpass.domain.feedback.repository.FeedbackRepository;
import com.careerpass.domain.feedback.repository.InterviewSessionRepository;
import com.careerpass.global.aws.service.S3Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewSessionService interviewSessionService;
    private final S3Service s3Service;

    // 🔹 파이썬 Resume / Interview AI 서버 WebClient
    private final WebClient aiWebClient;
    private final ObjectMapper objectMapper;

    // 피드백 생성
    @Transactional
    public Response create(CreateRequest req) {
        String title = generateDefaultTitle(req.userId(), req.feedbackType());
        Feedback feedback = Feedback.builder()
                .userId(req.userId())
                .title(title)
                .feedbackType(req.feedbackType())
                .totalScore(req.totalScore())
                .transcript(req.transcript())
                .feedbackText(req.feedbackText())
                .sectionFeedback(req.sectionFeedback())
                .introductionId(req.introductionId())
                .interviewId(req.interviewId())
                .questionId(req.questionId())
                .audioUrl(req.audioUrl())
                .durationMs(req.durationMs())
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        return toDto(saved);
    }

    // 자소서 AI 피드백 저장
    @Transactional
    public Response saveIntroductionFeedback(
            Long userId,
            Long introductionId,
            IntroFeedbackResponse aiRes
    ) {
        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "userId is required");
        }
        if (aiRes == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "AI response is null");
        }

        String sectionFeedback = toJson(Map.of(
                "originalResume", aiRes.originalResume(),
                "feedback", aiRes.feedback(),
                "regenResume", aiRes.regenResume(),
                "regenTossResume", aiRes.regenTossResume()
        ));

        Feedback feedback = Feedback.builder()
                .userId(userId)
                .title(generateDefaultTitle(userId, FeedbackType.INTRODUCTION))
                .feedbackType(FeedbackType.INTRODUCTION)
                .totalScore(0L)
                .transcript(null)
                .feedbackText(aiRes.feedback())
                .sectionFeedback(sectionFeedback)
                .introductionId(introductionId)
                .interviewId(null)
                .build();

        return toDto(feedbackRepository.save(feedback));
    }

    // 단일 피드백 조회
    @Transactional(readOnly = true)
    public Response get(Long id) {
        Feedback f = feedbackRepository.findById(id)
                .orElseThrow(() -> new com.careerpass.domain.feedback.exception.FeedbackNotFoundException(id));
        return toDto(f);
    }

    @Transactional
    public TitleResponse updateTitle(Long id, Long userId, String title) {
        Feedback f = feedbackRepository.findById(id)
                .orElseThrow(() -> new com.careerpass.domain.feedback.exception.FeedbackNotFoundException(id));
        if (!f.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        f.setTitle(title);
        return new TitleResponse(f.getId(), f.getTitle());
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Feedback f = feedbackRepository.findById(id)
                .orElseThrow(() -> new com.careerpass.domain.feedback.exception.FeedbackNotFoundException(id));
        if (!f.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        feedbackRepository.delete(f);
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

    // 본인 자기소개서 요약 리스트
    @Transactional(readOnly = true)
    public List<com.careerpass.domain.feedback.dto.FeedbackDtos.SummaryResponse> listMyIntroductionSummaries(Long userId) {
        return feedbackRepository.findByUserIdAndFeedbackTypeOrderByCreatedAtDesc(userId, FeedbackType.INTRODUCTION)
                .stream()
                .map(this::toSummaryDto)
                .toList();
    }

    // 본인 면접 요약 리스트
    @Transactional(readOnly = true)
    public List<com.careerpass.domain.feedback.dto.FeedbackDtos.SummaryResponse> listMyInterviewSummaries(Long userId) {
        return interviewSessionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, SessionStatus.COMPLETED)
                .stream()
                .map(session -> new com.careerpass.domain.feedback.dto.FeedbackDtos.SummaryResponse(
                        session.getId(),
                        session.getTitle(),
                        FeedbackType.INTERVIEW,
                        session.getAverageScore() == null ? null : Math.round(session.getAverageScore()),
                        session.getCreatedAt()
                ))
                .toList();
    }

    /**
     * 자소서 AI 피드백 생성 (파이썬 FastAPI 호출)
     */
    @Transactional(readOnly = true)
    public Mono<IntroFeedbackResponse> createIntroAiFeedback(IntroFeedbackDispatch dispatch) {

        String originalContent = dispatch.resumeContent();

        String cleanedContent = originalContent;

        cleanedContent = cleanedContent
                .replaceAll("[\r\n\t]+", " ");

        cleanedContent = cleanedContent
                .replaceAll("“", "\"").replaceAll("”", "\"")
                .replaceAll("‘", "'").replaceAll("’", "'");

        cleanedContent = cleanedContent
                .replaceAll("\\u00A0", " ")
                .replaceAll("\\u200B", " ");

        cleanedContent = cleanedContent.replaceAll(" {2,}", " ").trim();

        IntroFeedbackDispatch cleanDispatch = new IntroFeedbackDispatch(
                dispatch.userId(),
                cleanedContent
        );

        System.out.println("[AI 통신 시작] WebClient POST 요청을 Python AI 서버 (http://localhost:8088/resume/resume/feedback)로 보냅니다.");
        System.out.println("[요청 내용] userId: " + cleanDispatch.userId() + ", content length: " + cleanDispatch.resumeContent().length());

        return aiWebClient.post()
                .uri("/resume/resume/feedback")
                .bodyValue(cleanDispatch)
                .retrieve()
                .bodyToMono(IntroFeedbackResponse.class)
                .timeout(Duration.ofSeconds(60))
                .doOnError(WebClientResponseException.class, ex ->
                        System.err.println("[AI 통신 오류] Python 서버에서 4xx/5xx 응답: " + ex.getStatusCode() + ", Body: " + ex.getResponseBodyAsString()))
                .doOnError(WebClientRequestException.class, ex ->
                        System.err.println("[AI 통신 오류] Python 서버 연결 실패: " + ex.getMessage()))
                .doOnError(TimeoutException.class, ex ->
                        System.err.println("[AI 통신 실패] Python 응답 지연: " + ex.getMessage()))
                .onErrorMap(WebClientResponseException.class, ex ->
                        new ResponseStatusException(ex.getStatusCode(), "Python 서버 오류", ex))
                .onErrorMap(WebClientRequestException.class, ex ->
                        new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Python 서버 연결 실패", ex))
                .onErrorMap(TimeoutException.class, ex ->
                        new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Python 서버 응답 타임아웃", ex));
    }


    /**
     * 음성 파일을 받아 STT 변환, LLM AI 분석 후, 결과를 DB에 저장하는 메인 로직입니다.
     */
    @Transactional // 저장 로직이 포함되므로 기본적으로 @Transactional 유지
    public InterviewAiDtos.AnswerAnalysisResultDto processAnswerAudio(
            Long userId,
            InterviewAiDtos.SttRequestMetaDto meta,
            MultipartFile audioFile
    ) {
        log.info("[INTERVIEW_AI] 면접 음성 처리 시작: interviewId={}, userId={}, questionId={}, fileSize={}",
                meta.interviewId(),
                userId,
                meta.questionId(),
                (audioFile != null ? audioFile.getSize() : -1L)
        );

        if (audioFile == null || audioFile.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "INTERVIEW_AI_ERROR: 업로드된 면접 음성 파일이 비어 있습니다."
            );
        }

        String transcript = null;
        String sttStatus = "FAILED";
        try {
            InterviewAiDtos.SttResultDto sttResult = callSttServer(userId, meta, audioFile);
            boolean sttSuccess = sttResult != null
                    && sttResult.answerText() != null
                    && !sttResult.answerText().isBlank();
            if (sttSuccess) {
                transcript = sttResult.answerText();
                sttStatus = "SUCCESS";
            }
        } catch (Exception e) {
            log.warn("[INTERVIEW_AI] STT 실패. interviewId={}, userId={}, questionId={}",
                    meta.interviewId(), userId, meta.questionId(), e);
        }

        String audioUrl = null;
        try {
            audioUrl = s3Service.storeFile(audioFile);
        } catch (Exception e) {
            log.warn("[INTERVIEW_AI] 오디오 S3 업로드 실패. interviewId={}, userId={}, questionId={}",
                    meta.interviewId(), userId, meta.questionId(), e);
        }

        log.info("[INTERVIEW_AI] STT 텍스트 변환 완료. 길이: {}자. AI 분석을 시작합니다.",
                transcript.length());

        // 2. 최종 DTO 구성을 위한 데이터 준비
        // ⚠️ interviewId 가 null 이면 파이썬에는 0으로 보냄 (Pydantic 422 방지)
        Long safeInterviewId = (meta.interviewId() != null) ? meta.interviewId() : 0L;

        InterviewAiDtos.InterviewMetaDto metaDto = new InterviewAiDtos.InterviewMetaDto(
                safeInterviewId,
                userId,
                meta.jobApplied(),
                meta.questionId()
        );

        InterviewAiDtos.AnswerAnalysisResultDto rawAnalysisResult = null;
        if ("SUCCESS".equals(sttStatus)) {
            InterviewAiDtos.AnswerDispatchDto dispatchForAi = new InterviewAiDtos.AnswerDispatchDto(
                    0L, // answerId는 여기서는 사용하지 않음
                    meta.questionText(),
                    transcript,
                    meta.interviewDuration(),
                    meta.resumeContent(),
                    metaDto
            );

            // 3. AI 피드백 서버 호출 (실패해도 저장은 진행)
            try {
                rawAnalysisResult = runInterviewAnalysis(dispatchForAi);
            } catch (Exception e) {
                log.warn("[INTERVIEW_AI] 면접 AI 분석 실패. interviewId={}, userId={}, questionId={}",
                        meta.interviewId(), userId, meta.questionId(), e);
            }
        } else {
            log.warn("[INTERVIEW_AI] STT 실패로 AI 분석을 생략합니다. interviewId={}, userId={}, questionId={}",
                    meta.interviewId(), userId, meta.questionId());
        }

        InterviewSession session = interviewSessionService.getSessionOwned(userId, meta.interviewId());

        InterviewAiDtos.AnswerAnalysisResultDto finalResult = new InterviewAiDtos.AnswerAnalysisResultDto(
                sttStatus,
                transcript, // ⬅️ STT 텍스트를 첫 번째 필드에 채움
                rawAnalysisResult != null ? rawAnalysisResult.score() : null,
                rawAnalysisResult != null ? rawAnalysisResult.timeMs() : null,
                rawAnalysisResult != null && rawAnalysisResult.improvements() != null
                        ? rawAnalysisResult.improvements() : List.of(),
                rawAnalysisResult != null && rawAnalysisResult.strengths() != null
                        ? rawAnalysisResult.strengths() : List.of(),
                rawAnalysisResult != null && rawAnalysisResult.risks() != null
                        ? rawAnalysisResult.risks() : List.of(),
                session.getId(),
                audioUrl
        );

        log.info("[INTERVIEW_AI] AI 분석 완료. 점수: {}. DB 저장을 시작합니다.", finalResult.score());

        // 4. 면접 질문별 결과 DB 저장
        interviewSessionService.saveAnswer(session, meta, finalResult, audioUrl);

        log.info("[INTERVIEW_AI] DB 저장 완료. 최종 처리를 마칩니다.");

        return finalResult;
    }

    /**
     * Python STT 서버 (/voice/analyze)를 호출하여 음성 파일을 텍스트로 변환합니다.
     */
    private InterviewAiDtos.SttResultDto callSttServer(
            Long userId,
            InterviewAiDtos.SttRequestMetaDto meta,
            MultipartFile audioFile
    ) {
        try {
            if (audioFile == null || audioFile.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "STT_ERROR: 업로드된 파일이 비어 있습니다."
                );
            }

            MultipartBodyBuilder builder = new MultipartBodyBuilder();

            String metaJson = objectMapper.writeValueAsString(meta);
            builder.part("meta", metaJson, MediaType.APPLICATION_JSON);

            ByteArrayResource resource = new ByteArrayResource(audioFile.getBytes()) {
                @Override
                public String getFilename() {
                    return audioFile.getOriginalFilename();
                }
            };
            // contentType이 null일 가능성도 방어
            MediaType fileMediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (audioFile.getContentType() != null) {
                fileMediaType = MediaType.valueOf(audioFile.getContentType());
            }
            builder.part("file", resource, fileMediaType);

            log.info("[STT] sending audio to STT server...");
            InterviewAiDtos.SttResultDto sttResult = aiWebClient.post()
                    .uri("/voice/analyze")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(InterviewAiDtos.SttResultDto.class)
                    .block();

            boolean sttSuccess = sttResult != null
                    && sttResult.answerText() != null
                    && !sttResult.answerText().isBlank();
            log.info("[STT] response: success={}, text='{}'",
                    sttSuccess,
                    sttResult != null ? sttResult.answerText() : null);

            if (sttResult == null || sttResult.answerText() == null) {
                log.error("[STT] STT 서버에서 빈 결과를 반환했습니다. meta={}, userId={}", meta, userId);
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                        "STT_ERROR: STT 서버가 빈 결과를 반환했습니다."
                );
            }

            log.debug("[STT] STT 서버로부터 결과 수신 완료. 길이={}", sttResult.answerText().length());

            return sttResult;

        } catch (WebClientResponseException ex) {
            log.error("[STT] Python STT 서버 오류 응답: 상태={}, 본문={}, userId={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString(), userId, ex);
            throw new ResponseStatusException(
                    ex.getStatusCode(),
                    "STT_ERROR: " + ex.getResponseBodyAsString(),
                    ex
            );
        } catch (WebClientRequestException ex) {
            log.error("[STT] Python STT 서버 연결 실패, userId={}", userId, ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "STT_ERROR: STT 서버에 연결할 수 없습니다.",
                    ex
            );
        } catch (ResponseStatusException e) {
            // 위에서 직접 던진 STT_ERROR 유지
            throw e;
        } catch (Exception ex) {
            log.error("[STT] Python STT 서버 연결/실행 중 예외 발생, userId={}", userId, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "STT_ERROR: " + ex.getMessage(),
                    ex
            );
        }
    }

    /**
     * 면접 답변 AI 분석 (파이썬 FastAPI 호출)
     */
    @Transactional(readOnly = true)
    public InterviewAiDtos.AnswerAnalysisResultDto runInterviewAnalysis(InterviewAiDtos.AnswerDispatchDto dispatch) {

        String originalQuestionText = dispatch.questionText();
        String originalTranscript = dispatch.transcript();
        String originalResumeContent = dispatch.resumeContent();


        String cleanedQuestionText = originalQuestionText;
        if (cleanedQuestionText != null) {
            cleanedQuestionText = cleanedQuestionText
                    .replaceAll("[\r\n\t]+", " ")
                    .replaceAll("“", "\"").replaceAll("”", "\"")
                    .replaceAll("‘", "'").replaceAll("’", "'")
                    .replaceAll("\\u00A0", " ")
                    .replaceAll("\\u200B", " ")
                    .replaceAll(" {2,}", " ").trim();
        }

        String cleanedTranscript = originalTranscript;
        if (cleanedTranscript != null) {
            cleanedTranscript = cleanedTranscript
                    .replaceAll("[\r\n\t]+", " ")
                    .replaceAll("“", "\"").replaceAll("”", "\"")
                    .replaceAll("‘", "'").replaceAll("’", "'")
                    .replaceAll("\\u00A0", " ")
                    .replaceAll("\\u200B", " ")
                    .replaceAll(" {2,}", " ").trim();
        }

        String cleanedResumeContent = originalResumeContent;
        if (cleanedResumeContent != null) {
            cleanedResumeContent = cleanedResumeContent
                    .replaceAll("[\r\n\t]+", " ")
                    .replaceAll("“", "\"").replaceAll("”", "\"")
                    .replaceAll("‘", "'").replaceAll("’", "'")
                    .replaceAll("\\u00A0", " ")
                    .replaceAll("\\u200B", " ")
                    .replaceAll(" {2,}", " ").trim();
        }

        InterviewAiDtos.AnswerDispatchDto cleanDispatch = new InterviewAiDtos.AnswerDispatchDto(
                dispatch.answerId(),
                cleanedQuestionText,
                cleanedTranscript,
                dispatch.interviewDuration(),
                cleanedResumeContent,
                dispatch.meta()
        );

        log.debug("[INTERVIEW_AI] AI 서버로 JSON 요청 전송: URI=/interview/analysis/interview/run");

        try {
            InterviewAiDtos.AnswerAnalysisResultDto result = aiWebClient.post()
                    .uri("/interview/analysis/interview/run")
                    .bodyValue(cleanDispatch)
                    .retrieve()
                    .bodyToMono(InterviewAiDtos.AnswerAnalysisResultDto.class)
                    .block();

            if (result == null) {
                log.error("[INTERVIEW_AI] Python Interview AI 서버가 null 결과를 반환했습니다. dispatch={}", cleanDispatch);
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "AI_ERROR: 면접 AI 서버가 빈 결과를 반환했습니다."
                );
            }

            log.debug("[INTERVIEW_AI] AI 분석 서버로부터 결과 수신 완료.");
            return result;

        } catch (WebClientResponseException ex) {
            log.error("[INTERVIEW_AI] Python Interview AI 서버 오류 응답: 상태={}, 본문={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            throw new ResponseStatusException(
                    ex.getStatusCode(),
                    "AI_ERROR: " + ex.getResponseBodyAsString(),
                    ex
            );
        } catch (WebClientRequestException ex) {
            log.error("[INTERVIEW_AI] Python Interview AI 서버 연결 실패", ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "AI_ERROR: 면접 AI 서버에 연결할 수 없습니다.",
                    ex
            );
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception ex) {
            log.error("[INTERVIEW_AI] Python AI 서버 연결/실행 중 예외 발생", ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "AI_ERROR: " + ex.getMessage(),
                    ex
            );
        }
    }



    private Response toDto(Feedback f) {
        return new Response(
                f.getId(),
                f.getUserId(),
                f.getTitle(),
                f.getFeedbackType(),
                f.getTotalScore(),
                f.getTranscript(),
                f.getFeedbackText(),
                f.getSectionFeedback(),
                f.getIntroductionId(),
                f.getInterviewId(),
                f.getQuestionId(),
                f.getAudioUrl(),
                f.getDurationMs(),
                f.getCreatedAt()
        );
    }

    private com.careerpass.domain.feedback.dto.FeedbackDtos.SummaryResponse toSummaryDto(Feedback f) {
        return new com.careerpass.domain.feedback.dto.FeedbackDtos.SummaryResponse(
                f.getId(),
                f.getTitle(),
                f.getFeedbackType(),
                f.getTotalScore(),
                f.getCreatedAt()
        );
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 직렬화 실패", e);
        }
    }

    private String generateDefaultTitle(Long userId, FeedbackType feedbackType) {
        long next = feedbackRepository.countByUserIdAndFeedbackType(userId, feedbackType) + 1;
        String prefix = feedbackType == FeedbackType.INTRODUCTION ? "자기소개서" : "면접";
        String baseTitle = prefix + " " + next;

        if (feedbackRepository.existsByUserIdAndFeedbackTypeAndTitle(userId, feedbackType, baseTitle)) {
            String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            return baseTitle + " (" + suffix + ")";
        }

        return baseTitle;
    }
}
