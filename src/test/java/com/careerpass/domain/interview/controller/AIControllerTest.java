package com.careerpass.domain.interview.controller;

import com.careerpass.domain.interview.dto.AnalysisResultDto;
import com.careerpass.domain.interview.service.AIService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AIController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.careerpass\\.global\\.error\\..*"
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AIControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    AIService aiService;

    @Test
    @DisplayName("✅ meta(JSON) + file 업로드 성공 → 200")
    void analyze_ok_200() throws Exception {
        // meta 파트(JSON)
        String json = "{\"interviewId\":123,\"questionId\":7}";
        MockMultipartFile meta = new MockMultipartFile(
                "meta", "meta.json", "application/json", json.getBytes()
        );
        // 음성 파일
        MockMultipartFile file = new MockMultipartFile(
                "file", "voice.wav", "audio/wav", "bytes".getBytes()
        );

        // 서비스 성공 응답 목킹
        when(aiService.analyzeVoice(
                ArgumentMatchers.any(), ArgumentMatchers.any())
        ).thenReturn(AnalysisResultDto.builder()
                .questionId(7L).question("Q").answerText("A").score(8.0).build());

        mvc.perform(
                multipart("/api/interview/voice/analyze")
                        .file(meta)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().isOk());
    }

    @Test
    @DisplayName("❌ file 비어있으면 400")
    void analyze_emptyFile_400() throws Exception {
        String json = "{\"interviewId\":1,\"questionId\":1}";
        MockMultipartFile meta = new MockMultipartFile(
                "meta", "meta.json", "application/json", json.getBytes()
        );
        MockMultipartFile empty = new MockMultipartFile("file", new byte[0]);

        mvc.perform(
                multipart("/api/interview/voice/analyze")
                        .file(meta)
                        .file(empty)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("❌ multipart/form-data 아님 → 415")
    void analyze_notMultipart_415() throws Exception {
        mvc.perform(
                post("/api/interview/voice/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"meta\":{\"interviewId\":1,\"questionId\":1}}")
        ).andExpect(status().isUnsupportedMediaType());
    }
}