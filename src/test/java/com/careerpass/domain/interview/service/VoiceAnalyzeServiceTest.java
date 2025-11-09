package com.careerpass.domain.interview.service;

import com.careerpass.domain.interview.dto.VoiceAnalyzeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.nio.charset.StandardCharsets;

@ExtendWith(SpringExtension.class)
// ⚠️ 전체 애플리케이션 대신 아래 TestConfig만 로드
@ContextConfiguration(classes = VoiceAnalyzeServiceTest.TestConfig.class)
class VoiceAnalyzeServiceTest {

    // TestConfig에서 만든 Bean이 주입됨
    @org.springframework.beans.factory.annotation.Autowired
    private AIService aiService;

    @Configuration
    static class TestConfig {
        // FastAPI 기본 주소 (필요시 변경)
        @Bean
        WebClient aiWebClient() {
            return WebClient.builder().baseUrl("http://localhost:5001").build();
        }
        @Bean
        AIService aiService(WebClient aiWebClient) {
            return new AIService(aiWebClient);
        }
    }

    @Test
    @DisplayName("❌ 파일이 비어있을 때 예외 발생")
    void testEmptyFile() {
        MultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);
        Mono<VoiceAnalyzeResponse> result = aiService.analyzeVoice(emptyFile);
        StepVerifier.create(result)
                .expectErrorMatches(ex -> ex.getMessage().contains("비어"))
                .verify();
    }

    @Test
    @DisplayName("❌ 잘못된 확장자 파일일 때 예외 발생")
    void testInvalidExtension() {
        MultipartFile invalidFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "dummy".getBytes(StandardCharsets.UTF_8)
        );
        Mono<VoiceAnalyzeResponse> result = aiService.analyzeVoice(invalidFile);
        StepVerifier.create(result)
                .expectErrorMatches(ex -> ex.getMessage().contains("형식"))
                .verify();
    }

    @Test
    @DisplayName("⚠️ FastAPI 미기동 시: 네트워크 오류 → RuntimeException")
    void testServerNotRunning() {
        MultipartFile validFile = new MockMultipartFile(
                "file", "voice.m4a", "audio/m4a", "fake".getBytes(StandardCharsets.UTF_8)
        );
        StepVerifier.create(aiService.analyzeVoice(validFile))
                .expectErrorMatches(ex -> ex.getMessage().contains("AI 서버 호출 실패"))
                .verify();
    }

//    //  ✅ FastAPI 켜져 있을 때만 사용
//     @Test
//     @DisplayName("✅ 정상 파일 업로드 시 성공 응답")
//     void testValid_whenFastApiRunning() {
//         MultipartFile valid = new MockMultipartFile("file","ok.m4a","audio/m4a","fake".getBytes());
//         StepVerifier.create(aiService.analyzeVoice(valid))
//                 .expectNextMatches(res -> res != null && res.isOk())
//                 .verifyComplete();
//     }

    @Test
    @DisplayName("✅ 성공 경로: AI 서버 200 OK → 정상 매핑 (MockWebServer)")
    void testSuccess_withMockServer() throws Exception {
        // 1) Mock 서버 준비
        MockWebServer server = new MockWebServer();
        server.start();
        String baseUrl = server.url("/").toString();

        // 2) 가짜 성공 응답 세팅
        String body = """
        {
          "ok": true,
          "filename": "ok.m4a",
          "text": "안녕하세요.",
          "segments": [{"start":0.0,"end":1.2,"text":"안녕하세요."}],
          "summary": "로컬 Whisper로 STT 완료 ✅"
        }
        """;
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        // 3) 테스트용 WebClient/Service 생성 (실 서버 대신 Mock 주소)
        WebClient testClient = WebClient.builder().baseUrl(baseUrl).build();
        AIService testService = new AIService(testClient);

        // 4) 임시 “진짜처럼 보이는” 파일(아무 바이트 OK, 왜냐면 여기선 FastAPI 안 씀)
        MultipartFile dummy = new MockMultipartFile(
                "file", "ok.m4a", "audio/m4a", "not-checked".getBytes()
        );

        StepVerifier.create(testService.analyzeVoice(dummy))
                .expectNextMatches(res ->
                        res != null &&
                                res.isOk() &&
                                "안녕하세요.".equals(res.getText()))
                .verifyComplete();

        server.shutdown();
    }
}