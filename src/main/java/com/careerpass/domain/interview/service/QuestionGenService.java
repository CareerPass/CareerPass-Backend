package com.careerpass.domain.interview.service;

import com.careerpass.domain.feedback.dto.IntroductionAiDtos;
import com.careerpass.domain.interview.dto.GenerateQuestionsRequest;
import com.careerpass.domain.interview.dto.GenerateQuestionsResponse;
import com.careerpass.domain.interview.dto.QuestionItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionGenService {

    // WebClientConfig 에서 만든 Bean (질문 AI 서버: http://localhost:5002)
    private final WebClient aiWebClient;

    /**
     * 🎯 userId + coverLetter 기반으로 질문 생성
     * 1) userId → 전공/직무 조회
     * 2) AI 서버(question_ai.py) 호출
     * 3) String 리스트 → QuestionItemDto 리스트로 변환
     * 4) GenerateQuestionsResponse 로 래핑
     */
    public Mono<GenerateQuestionsResponse> generate(GenerateQuestionsRequest req) {

        String originalCoverLetter = req.coverLetter();

        String cleanedCoverLetter = originalCoverLetter;

        cleanedCoverLetter = cleanedCoverLetter
                .replaceAll("[\r\n\t]+", " ");

        cleanedCoverLetter = cleanedCoverLetter
                .replaceAll("“", "\"").replaceAll("”", "\"")
                .replaceAll("‘", "'").replaceAll("’", "'");

        cleanedCoverLetter = cleanedCoverLetter
                .replaceAll("\\u00A0", " ")
                .replaceAll("\\u200B", " ");

        cleanedCoverLetter = cleanedCoverLetter.replaceAll(" {2,}", " ").trim();

        // 1) userId 로 전공/직무 매핑 (지금은 임시 하드코딩)
        MajorJobInfo info = resolveMajorAndJob(req.userId());

        // 2) AI 서버로 보낼 요청 바디 (major, job_title, cover_letter)
        AiQuestionRequest aiReq = new AiQuestionRequest(
                info.major(),
                info.jobTitle(),
                req.coverLetter()
        );

        // 3) Flask 질문 생성 서버 호출
        return aiWebClient.post()
                .uri("/question/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(aiReq)
                .retrieve()
                .bodyToMono(QuestionAiResponse.class)        // { "questions": [ "질문1", ... ] }
                .map(res -> {

                    List<String> questionsFromAi = res.questions();

                    // 4) String → QuestionItemDto
                    List<QuestionItemDto> questionItems =
                            IntStream.range(0, questionsFromAi.size())
                                    .mapToObj(i -> QuestionItemDto.builder()
                                            .questionId("q-" + (i + 1))     // 임시 ID
                                            .text(questionsFromAi.get(i))   // 질문 텍스트
                                            .build())
                                    .toList();

                    // 5) 최종 응답 DTO 조립
                    return GenerateQuestionsResponse.builder()
                            .major(info.major())
                            .jobTitle(info.jobTitle())
                            .generatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                            .questions(questionItems)
                            .build();
                });
    }

    /**
     * ✅ userId로부터 major/jobTitle 조회하는 부분
     * 지금은 임시 값이고, 나중에 User/학습프로필 엔티티에서 꺼내면 됨.
     */
    private MajorJobInfo resolveMajorAndJob(Long userId) {
        // TODO: 실제 구현에서는 userId 로 DB 조회
        // ex) User user = userService.findById(userId);
        //     return new MajorJobInfo(user.getMajor(), user.getTargetJob());
        log.info("임시 major/jobTitle 사용 (userId={})", userId);
        return new MajorJobInfo("컴퓨터공학과", "백엔드 개발자");
    }

    /**
     * 🔹 AI(question_ai.py)가 반환하는 JSON 형식
     * { "questions": ["질문1", "질문2", ...] }
     */
    private record QuestionAiResponse(@com.fasterxml.jackson.annotation.JsonProperty("question")
                                      List<String> questions) {}

    /**
     * 🔹 질문 AI 서버로 보내는 요청 JSON 형식
     * { "major": "...", "job_title": "...", "cover_letter": "..." }
     */
    private record AiQuestionRequest(
            String major,
            @com.fasterxml.jackson.annotation.JsonProperty("job_title")
            String jobTitle,
            @com.fasterxml.jackson.annotation.JsonProperty("cover_letter")
            String coverLetter
    ) {}

    /**
     * 🔹 내부에서만 쓰는 전공/직무 묶음
     */
    private record MajorJobInfo(String major, String jobTitle) {}
}
