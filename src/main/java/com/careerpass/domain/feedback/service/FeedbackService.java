package com.careerpass.domain.feedback.service;

import com.careerpass.domain.feedback.dto.FeedbackDtos.CreateRequest;
import com.careerpass.domain.feedback.dto.FeedbackDtos.Response;
import com.careerpass.domain.feedback.dto.InterviewAiDtos;
import com.careerpass.domain.feedback.dto.IntroductionAiDtos.IntroFeedbackRequest;
import com.careerpass.domain.feedback.dto.IntroductionAiDtos.IntroFeedbackResponse;
import com.careerpass.domain.feedback.entity.Feedback;
import com.careerpass.domain.feedback.entity.FeedbackType;
import com.careerpass.domain.feedback.repository.FeedbackRepository;
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
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    // 🔹 파이썬 Resume / Interview AI 서버 WebClient
    private final WebClient aiWebClient;
    private final ObjectMapper objectMapper;

    // 피드백 생성
    @Transactional
    public Response create(CreateRequest req) {
        Feedback feedback = Feedback.builder()
                .title(req.title())
                .feedbackType(req.feedbackType())
                .totalScore(req.totalScore())
                .transcript(req.transcript())
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
    public Mono<IntroFeedbackResponse> createIntroAiFeedback(IntroFeedbackRequest req) {

        String originalContent = req.resumeContent();

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

        IntroFeedbackRequest cleanReq = new IntroFeedbackRequest(
                req.userId(),
                cleanedContent
        );

        System.out.println("[AI 통신 시작] WebClient POST 요청을 Python AI 서버 (http://localhost:8088/resume/resume/feedback)로 보냅니다.");
        System.out.println("[요청 내용] userId: " + cleanReq.userId() + ", content length: " + cleanReq.resumeContent().length());

        return aiWebClient.post()
                .uri("/resume/resume/feedback")
                .bodyValue(cleanReq)
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
            InterviewAiDtos.SttRequestMetaDto meta,
            MultipartFile audioFile
    ) {
        log.info("[INTERVIEW_AI] 면접 음성 처리 시작: interviewId={}, userId={}, questionId={}, fileSize={}",
                meta.interviewId(),
                meta.userId(),
                meta.questionId(),
                (audioFile != null ? audioFile.getSize() : -1L)
        );

        try {
            if (audioFile == null || audioFile.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "INTERVIEW_AI_ERROR: 업로드된 면접 음성 파일이 비어 있습니다."
                );
            }

            // 1. STT 서버 호출 및 텍스트 변환
            String transcript = callSttServer(meta, audioFile);

            if (transcript == null || transcript.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "INTERVIEW_AI_ERROR: STT 결과가 비어 있습니다."
                );
            }

            log.info("[INTERVIEW_AI] STT 텍스트 변환 완료. 길이: {}자. AI 분석을 시작합니다.",
                    transcript.length());

            // 2. 최종 DTO 구성을 위한 데이터 준비
            InterviewAiDtos.InterviewMetaDto metaDto = new InterviewAiDtos.InterviewMetaDto(
                    meta.interviewId(),
                    meta.userId(),
                    meta.jobApplied(),
                    meta.questionId()
            );

            InterviewAiDtos.AnswerDispatchDto dispatchForAi = new InterviewAiDtos.AnswerDispatchDto(
                    0L, // answerId는 여기서는 사용하지 않음
                    meta.questionText(),
                    transcript,
                    meta.resumeContent(),
                    metaDto
            );

            // 3. AI 피드백 서버 호출
            InterviewAiDtos.AnswerAnalysisResultDto rawAnalysisResult = analyzeInterviewAnswer(dispatchForAi);

            if (rawAnalysisResult == null) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "INTERVIEW_AI_ERROR: 면접 AI 분석 결과가 null 입니다."
                );
            }

            InterviewAiDtos.AnswerAnalysisResultDto finalResult = new InterviewAiDtos.AnswerAnalysisResultDto(
                    transcript, // ⬅️ STT 텍스트를 첫 번째 필드에 채움
                    rawAnalysisResult.score(),
                    rawAnalysisResult.timeMs(),
                    rawAnalysisResult.fluency(),
                    rawAnalysisResult.contentDepth(),
                    rawAnalysisResult.structure(),
                    rawAnalysisResult.fillerCount(),
                    rawAnalysisResult.improvements(),
                    rawAnalysisResult.strengths(),
                    rawAnalysisResult.risks()
            );

            log.info("[INTERVIEW_AI] AI 분석 완료. 점수: {}. DB 저장을 시작합니다.", finalResult.score());

            // 4. 피드백 결과 DB 저장
            saveInterviewFeedback(meta, finalResult);

            log.info("[INTERVIEW_AI] DB 저장 완료. 최종 처리를 마칩니다.");

            return finalResult;

        } catch (ResponseStatusException e) {
            // 이미 의미 있는 메시지를 가진 예외는 그대로 전달
            log.error("[INTERVIEW_AI] ResponseStatusException 발생: status={}, reason={}",
                    e.getStatusCode(), e.getReason(), e);
            throw e;
        } catch (Exception e) {
            // 나머지는 공통 인터뷰 AI 에러로 래핑
            log.error("[INTERVIEW_AI] 처리 중 예외 발생 - interviewId={}, userId={}, questionId={}",
                    meta.interviewId(), meta.userId(), meta.questionId(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "INTERVIEW_AI_ERROR: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Python STT 서버 (/voice/analyze)를 호출하여 음성 파일을 텍스트로 변환합니다.
     */
    private String callSttServer(
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

            InterviewAiDtos.SttResultDto sttResult = aiWebClient.post()
                    .uri("/voice/analyze")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(InterviewAiDtos.SttResultDto.class)
                    .block();

            if (sttResult == null || sttResult.answerText() == null) {
                log.error("[STT] STT 서버에서 빈 결과를 반환했습니다. meta={}", meta);
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "STT_ERROR: STT 서버가 빈 결과를 반환했습니다."
                );
            }

            log.debug("[STT] STT 서버로부터 결과 수신 완료. 길이={}", sttResult.answerText().length());

            return sttResult.answerText();

        } catch (WebClientResponseException ex) {
            log.error("[STT] Python STT 서버 오류 응답: 상태={}, 본문={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            throw new ResponseStatusException(
                    ex.getStatusCode(),
                    "STT_ERROR: " + ex.getResponseBodyAsString(),
                    ex
            );
        } catch (WebClientRequestException ex) {
            log.error("[STT] Python STT 서버 연결 실패", ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "STT_ERROR: STT 서버에 연결할 수 없습니다.",
                    ex
            );
        } catch (ResponseStatusException e) {
            // 위에서 직접 던진 STT_ERROR 유지
            throw e;
        } catch (Exception ex) {
            log.error("[STT] Python STT 서버 연결/실행 중 예외 발생", ex);
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
    public InterviewAiDtos.AnswerAnalysisResultDto analyzeInterviewAnswer(InterviewAiDtos.AnswerDispatchDto dispatch) {

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


    /**
     * AI 분석 결과를 Feedback 엔티티로 변환하여 저장합니다.
     */
    private void saveInterviewFeedback(
            InterviewAiDtos.SttRequestMetaDto meta,
            InterviewAiDtos.AnswerAnalysisResultDto finalResult
    ) {
        // 1. 전체 피드백 텍스트 구성 (improvements + strengths + risks)
        String combinedFeedbackText = formatFeedbackText(finalResult);

        // 2. 항목별 피드백 (structure, contentDepth, fluency)를 JSON 또는 문자열로 구성
        String sectionFeedback = String.format(
                "{\"fluency\": %d, \"contentDepth\": %d, \"structure\": %d, \"fillerCount\": %d}",
                finalResult.fluency(),
                finalResult.contentDepth(),
                finalResult.structure(),
                finalResult.fillerCount()
        );

        // 3. Feedback 엔티티 빌드
        Feedback feedback = Feedback.builder()
                .title(meta.questionText()) // 질문을 제목으로 사용
                .feedbackType(FeedbackType.INTERVIEW) // 면접 타입 지정
                .totalScore(finalResult.score().longValue()) // 점수를 Long으로 변환
                .transcript(finalResult.transcript())
                .feedbackText(combinedFeedbackText)
                .sectionFeedback(sectionFeedback)
                .introductionId(null) // 면접 피드백이므로 NULL
                .interviewId(meta.interviewId()) // Interview ID 사용
                .build();

        feedbackRepository.save(feedback);
    }

    /**
     * 피드백 리스트들을 읽기 쉬운 하나의 문자열로 포맷팅합니다.
     */
    private String formatFeedbackText(InterviewAiDtos.AnswerAnalysisResultDto analysisResult) {
        StringBuilder sb = new StringBuilder();

        sb.append("--- 개선 사항 (Improvements) ---\n");
        sb.append(analysisResult.improvements().stream()
                .map(s -> "* " + s)
                .collect(Collectors.joining("\n")));

        sb.append("\n\n--- 강점 (Strengths) ---\n");
        sb.append(analysisResult.strengths().stream()
                .map(s -> "* " + s)
                .collect(Collectors.joining("\n")));

        if (analysisResult.risks() != null && !analysisResult.risks().isEmpty()) {
            sb.append("\n\n--- 위험 요소 (Risks) ---\n");
            sb.append(analysisResult.risks().stream()
                    .map(s -> "* " + s)
                    .collect(Collectors.joining("\n")));
        }

        return sb.toString();
    }

    private Response toDto(Feedback f) {
        return new Response(
                f.getId(),
                f.getTitle(),
                f.getFeedbackType(),
                f.getTotalScore(),
                f.getTranscript(),
                f.getFeedbackText(),
                f.getSectionFeedback(),
                f.getIntroductionId(),
                f.getInterviewId()
        );
    }
}