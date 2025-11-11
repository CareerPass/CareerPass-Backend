package com.careerpass.domain.interview.service;

import com.careerpass.domain.interview.dto.AnswerUploadMetaDto;
import com.careerpass.domain.interview.dto.AnalysisResultDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class AIServiceTest {

    private AIService newService(String baseUrl) {
        WebClient client = WebClient.builder().baseUrl(baseUrl).build();
        return new AIService(client, new ObjectMapper());
    }

    private AnswerUploadMetaDto meta(long interviewId, long questionId) {
        return AnswerUploadMetaDto.builder()
                .interviewId(interviewId)
                .questionId(questionId)
                .build();
    }

    @Test
    @DisplayName("❌ 파일이 비어있으면 IllegalArgumentException")
    void emptyFile_throws() {
        AIService svc = newService("http://localhost:5001"); // 사용 안됨
        MultipartFile empty = new MockMultipartFile("file", new byte[0]);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> svc.analyzeVoice(meta(1, 1), empty)
        );
        assertTrue(ex.getMessage().contains("비어"));
    }

    @Test
    @DisplayName("❌ 잘못된 확장자면 IllegalArgumentException")
    void invalidExt_throws() {
        AIService svc = newService("http://localhost:5001");
        MultipartFile txt = new MockMultipartFile(
                "file", "wrong.txt", "text/plain", "dummy".getBytes(StandardCharsets.UTF_8)
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> svc.analyzeVoice(meta(1, 1), txt)
        );
        assertTrue(ex.getMessage().contains("형식"));
    }

    @Test
    @DisplayName("⚠️ FastAPI 미기동 시: RuntimeException (네트워크 실패)")
    void serverDown_runtimeException() {
        // 존재하지 않는 포트로 호출해서 실패 유도
        AIService svc = newService("http://localhost:59999");
        MultipartFile ok = new MockMultipartFile(
                "file", "voice.m4a", "audio/m4a", "fake".getBytes(StandardCharsets.UTF_8)
        );

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> svc.analyzeVoice(meta(123, 7), ok)
        );
        assertTrue(ex.getMessage().contains("AI 서버 호출 실패"));
    }

    @Test
    @DisplayName("✅ 성공 경로: 200 OK → AnalysisResultDto 매핑 (MockWebServer)")
    void success_withMockServer() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.start();
            String baseUrl = server.url("/").toString();

            // FastAPI가 반환할 JSON (서비스는 AnalysisResultDto로 매핑)
            String body = """
            {
              "questionId": 7,
              "question": "자기소개 부탁드립니다.",
              "answerText": "안녕하세요, 백엔드 개발자 지망생입니다.",
              "score": 8.5,
              "feedback": "구체적 사례를 한 문장 추가하면 더 좋습니다.",
              "durationMs": 5231
            }
            """;
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(body));

            AIService svc = newService(baseUrl);
            MultipartFile ok = new MockMultipartFile(
                    "file", "voice.wav", "audio/wav", "bytes-dont-matter".getBytes()
            );

            AnalysisResultDto res = svc.analyzeVoice(meta(123, 7), ok);

            assertNotNull(res);
            assertEquals(7L, res.getQuestionId());
            assertEquals("자기소개 부탁드립니다.", res.getQuestion());
            assertEquals("안녕하세요, 백엔드 개발자 지망생입니다.", res.getAnswerText());
            assertEquals(8.5, res.getScore());
            assertTrue(res.getFeedback().contains("구체적 사례"));
        }
    }

    @Test
    @DisplayName("🟥 FastAPI 400 → RuntimeException 매핑")
    void ai_400_mapsRuntime() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setResponseCode(400)
                    .setHeader("Content-Type", "application/json")
                    .setBody("{\"detail\":\"bad input\"}"));
            server.start();

            AIService svc = newService(server.url("/").toString());
            MultipartFile ok = new MockMultipartFile("file","v.wav","audio/wav","x".getBytes());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> svc.analyzeVoice(meta(1,1), ok));
            assertTrue(ex.getMessage().contains("AI 서버"));
        }
    }

    @Test
    @DisplayName("🟥 응답 JSON 필수 필드 누락 → RuntimeException")
    void responseMissingFields_throws() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody("{\"score\": 9.1}")); // questionId/answerText 없음
            server.start();

            AIService svc = newService(server.url("/").toString());
            MultipartFile ok = new MockMultipartFile("file","v.wav","audio/wav","x".getBytes());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> svc.analyzeVoice(meta(1,1), ok));
            assertTrue(ex.getMessage().contains("응답") || ex.getMessage().contains("불완전"));
        }
    }

    @Test
    @DisplayName("🟥 FastAPI 413 → RuntimeException 매핑(파일 너무 큼)")
    void ai_413_mapsRuntime() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setResponseCode(413)
                    .setHeader("Content-Type", "application/json")
                    .setBody("{\"detail\":\"too large\"}"));
            server.start();

            AIService svc = newService(server.url("/").toString());
            MultipartFile ok = new MockMultipartFile("file","v.wav","audio/wav","x".getBytes());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> svc.analyzeVoice(meta(1,1), ok));
            assertTrue(ex.getMessage().contains("파일이 너무 큽니다") || ex.getMessage().contains("AI 서버"));
        }
    }
}