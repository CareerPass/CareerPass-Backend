package com.careerpass.domain.feedback.service;

import com.careerpass.domain.feedback.dto.InterviewAiDtos;
import com.careerpass.domain.feedback.dto.InterviewSessionDtos;
import com.careerpass.domain.feedback.entity.InterviewAnswer;
import com.careerpass.domain.feedback.entity.InterviewSession;
import com.careerpass.domain.feedback.entity.SessionStatus;
import com.careerpass.domain.feedback.repository.InterviewAnswerRepository;
import com.careerpass.domain.feedback.repository.InterviewSessionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewSessionService {

    private static final int SUMMARY_ITEM_LIMIT = 5;
    private static final TypeReference<List<String>> LIST_STRING_TYPE = new TypeReference<>() {};

    private final InterviewSessionRepository sessionRepository;
    private final InterviewAnswerRepository answerRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public InterviewSession createSession(Long userId, String jobApplied) {
        if (jobApplied == null || jobApplied.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "jobApplied is required");
        }

        long nextIndex = sessionRepository.countByUserIdAndStatus(userId, SessionStatus.COMPLETED) + 1;
        InterviewSession session = InterviewSession.builder()
                .userId(userId)
                .jobApplied(jobApplied)
                .title("면접 " + nextIndex)
                .status(SessionStatus.IN_PROGRESS)
                .build();

        return sessionRepository.save(session);
    }

    @Transactional
    public InterviewAnswer saveAnswer(
            InterviewSession session,
            InterviewAiDtos.SttRequestMetaDto meta,
            InterviewAiDtos.AnswerAnalysisResultDto result,
            String audioUrl
    ) {
        InterviewAnswer answer = InterviewAnswer.builder()
                .sessionId(session.getId())
                .questionId(meta.questionId())
                .questionText(meta.questionText())
                .transcript(result.transcript())
                .score(result.score())
                .timeMs(result.timeMs())
                .audioUrl(audioUrl)
                .strengths(toJson(result.strengths()))
                .improvements(toJson(result.improvements()))
                .risks(toJson(result.risks()))
                .build();

        updateSessionDurationIfNeeded(session, meta.interviewDuration());

        return answerRepository.save(answer);
    }

    @Transactional
    public InterviewSessionDtos.InterviewResultSheetDto finalizeSession(
            Long userId,
            Long interviewId,
            Long totalDurationSec
    ) {
        InterviewSession session = getSessionOwned(userId, interviewId);
        List<InterviewAnswer> answers = answerRepository.findBySessionIdOrderByIdAsc(interviewId);
        if (answers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Interview answers are missing");
        }

        SummaryAggregate aggregate = summarize(answers);
        Long resolvedDuration = resolveTotalDurationSec(totalDurationSec, session, answers, true);

        session.setQuestionCount(answers.size());
        session.setAverageScore(aggregate.averageScore());
        session.setTotalDurationSec(resolvedDuration);
        session.setOverallStrengths(toJson(aggregate.overallStrengths()));
        session.setOverallImprovements(toJson(aggregate.overallImprovements()));
        session.setOverallRisks(toJson(aggregate.overallRisks()));
        session.setStatus(SessionStatus.COMPLETED);

        InterviewSessionDtos.InterviewSessionSummaryWithOverallDto summary =
                toSummaryWithOverallDto(session, aggregate.overallStrengths(), aggregate.overallImprovements(),
                        aggregate.overallRisks());
        List<InterviewSessionDtos.InterviewAnswerDto> answerDtos = toAnswerDtos(answers);

        return new InterviewSessionDtos.InterviewResultSheetDto(summary, answerDtos);
    }

    @Transactional(readOnly = true)
    public InterviewSessionDtos.InterviewResultSheetDto getResultSheet(Long userId, Long interviewId) {
        InterviewSession session = getSessionOwned(userId, interviewId);
        List<InterviewAnswer> answers = answerRepository.findBySessionIdOrderByIdAsc(interviewId);

        SummaryAggregate aggregate = summarize(answers);
        Long resolvedDuration = resolveTotalDurationSec(null, session, answers, false);

        List<String> strengths = session.getOverallStrengths() != null
                ? parseList(session.getOverallStrengths())
                : aggregate.overallStrengths();
        List<String> improvements = session.getOverallImprovements() != null
                ? parseList(session.getOverallImprovements())
                : aggregate.overallImprovements();
        List<String> risks = session.getOverallRisks() != null
                ? parseList(session.getOverallRisks())
                : aggregate.overallRisks();

        Double averageScore = session.getAverageScore() != null
                ? session.getAverageScore()
                : aggregate.averageScore();

        Integer questionCount = session.getQuestionCount() != null
                ? session.getQuestionCount()
                : answers.size();

        InterviewSessionDtos.InterviewSessionSummaryWithOverallDto summary =
                new InterviewSessionDtos.InterviewSessionSummaryWithOverallDto(
                        session.getId(),
                        session.getCreatedAt(),
                        session.getTitle(),
                        session.getJobApplied(),
                        resolvedDuration,
                        questionCount,
                        averageScore,
                        strengths,
                        improvements,
                        risks
                );

        List<InterviewSessionDtos.InterviewAnswerDto> answerDtos = toAnswerDtos(answers);
        return new InterviewSessionDtos.InterviewResultSheetDto(summary, answerDtos);
    }

    @Transactional(readOnly = true)
    public List<InterviewSessionDtos.InterviewSessionSummaryDto> listHistory(Long userId) {
        return sessionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, SessionStatus.COMPLETED)
                .stream()
                .map(this::toSummaryDto)
                .toList();
    }

    @Transactional
    public void deleteSession(Long userId, Long interviewId) {
        InterviewSession session = getSessionOwned(userId, interviewId);
        answerRepository.deleteBySessionId(session.getId());
        sessionRepository.delete(session);
    }

    @Transactional
    public InterviewSessionDtos.TitleResponse updateTitle(Long userId, Long interviewId, String title) {
        InterviewSession session = getSessionOwned(userId, interviewId);
        session.setTitle(title);
        return new InterviewSessionDtos.TitleResponse(session.getId(), session.getTitle());
    }

    public InterviewSession getSessionOwned(Long userId, Long interviewId) {
        return sessionRepository.findByIdAndUserId(interviewId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Interview session not found"
                ));
    }

    private SummaryAggregate summarize(List<InterviewAnswer> answers) {
        List<Integer> scores = new ArrayList<>();
        List<List<String>> strengths = new ArrayList<>();
        List<List<String>> improvements = new ArrayList<>();
        List<List<String>> risks = new ArrayList<>();

        for (InterviewAnswer answer : answers) {
            if (answer.getScore() != null) {
                scores.add(answer.getScore());
            }
            strengths.add(parseList(answer.getStrengths()));
            improvements.add(parseList(answer.getImprovements()));
            risks.add(parseList(answer.getRisks()));
        }

        double averageScore = scores.isEmpty()
                ? 0.0
                : scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);

        return new SummaryAggregate(
                averageScore,
                mergeDistinctLimited(strengths),
                mergeDistinctLimited(improvements),
                mergeDistinctLimited(risks)
        );
    }

    private InterviewSessionDtos.InterviewSessionSummaryDto toSummaryDto(InterviewSession session) {
        return new InterviewSessionDtos.InterviewSessionSummaryDto(
                session.getId(),
                session.getCreatedAt(),
                session.getTitle(),
                session.getJobApplied(),
                session.getTotalDurationSec(),
                session.getQuestionCount(),
                session.getAverageScore()
        );
    }

    private InterviewSessionDtos.InterviewSessionSummaryWithOverallDto toSummaryWithOverallDto(
            InterviewSession session,
            List<String> strengths,
            List<String> improvements,
            List<String> risks
    ) {
        return new InterviewSessionDtos.InterviewSessionSummaryWithOverallDto(
                session.getId(),
                session.getCreatedAt(),
                session.getTitle(),
                session.getJobApplied(),
                session.getTotalDurationSec(),
                session.getQuestionCount(),
                session.getAverageScore(),
                strengths,
                improvements,
                risks
        );
    }

    private List<InterviewSessionDtos.InterviewAnswerDto> toAnswerDtos(List<InterviewAnswer> answers) {
        return answers.stream()
                .map(answer -> new InterviewSessionDtos.InterviewAnswerDto(
                        answer.getQuestionId(),
                        answer.getQuestionText(),
                        answer.getTranscript(),
                        answer.getScore(),
                        answer.getTimeMs(),
                        answer.getAudioUrl(),
                        parseList(answer.getStrengths()),
                        parseList(answer.getImprovements()),
                        parseList(answer.getRisks())
                ))
                .toList();
    }

    private void updateSessionDurationIfNeeded(InterviewSession session, Long interviewDurationMs) {
        if (interviewDurationMs == null) {
            return;
        }
        long durationSec = Math.max(0L, interviewDurationMs / 1000);
        Long current = session.getTotalDurationSec();
        if (current == null || durationSec > current) {
            session.setTotalDurationSec(durationSec);
        }
    }

    private Long resolveTotalDurationSec(
            Long requestedDurationSec,
            InterviewSession session,
            List<InterviewAnswer> answers,
            boolean preferAnswerSum
    ) {
        if (requestedDurationSec != null) {
            return requestedDurationSec;
        }
        long sumMs = answers.stream()
                .map(InterviewAnswer::getTimeMs)
                .filter(value -> value != null && value > 0)
                .mapToLong(Long::longValue)
                .sum();
        long sumSec = Math.max(0L, sumMs / 1000);
        if (preferAnswerSum) {
            if (sumSec > 0) {
                return sumSec;
            }
            return session.getTotalDurationSec() != null ? session.getTotalDurationSec() : 0L;
        }
        if (session.getTotalDurationSec() != null) {
            return session.getTotalDurationSec();
        }
        return sumSec;
    }

    private List<String> mergeDistinctLimited(List<List<String>> groups) {
        Set<String> merged = new LinkedHashSet<>();
        for (List<String> group : groups) {
            if (group == null) {
                continue;
            }
            for (String item : group) {
                if (item == null || item.isBlank()) {
                    continue;
                }
                merged.add(item.trim());
                if (merged.size() >= SUMMARY_ITEM_LIMIT) {
                    break;
                }
            }
            if (merged.size() >= SUMMARY_ITEM_LIMIT) {
                break;
            }
        }
        return new ArrayList<>(merged);
    }

    private List<String> parseList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, LIST_STRING_TYPE);
        } catch (Exception e) {
            log.warn("Failed to parse interview list json, json={}", json);
            return List.of();
        }
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "JSON serialization failed", e);
        }
    }

    private record SummaryAggregate(
            double averageScore,
            List<String> overallStrengths,
            List<String> overallImprovements,
            List<String> overallRisks
    ) {}
}
