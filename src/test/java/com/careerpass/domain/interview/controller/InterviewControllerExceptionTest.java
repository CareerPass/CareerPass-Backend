package com.careerpass.domain.interview.controller;

import com.careerpass.domain.interview.entity.Interview;
import com.careerpass.domain.interview.service.InterviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = InterviewController.class,
        // ✅ 전역 예외 핸들러를 스캔에서 아예 제외 (충돌 방지)
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.careerpass\\.global\\.error\\..*"
        )
)
@AutoConfigureMockMvc(addFilters = false) // ✅ 시큐리티 필터 비활성화
class InterviewControllerExceptionTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    InterviewService interviewService;

    // 혹시라도 내부에서 참조될 수 있으니 Mock만 준비(실제론 호출 안 함)
    @MockBean
    com.careerpass.global.aws.service.S3Service s3Service;

    @Test
    @DisplayName("빈 파일이면 400")
    void emptyFile_returns400() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("file", new byte[0]);

        mvc.perform(
                multipart("/interview/audio")
                        .file(empty)
                        .param("userId", "1")
                        .param("jobApplied", "BE")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("multipart/form-data 아님 → 415")
    void notMultipart_returns415() throws Exception {
        mvc.perform(
                post("/interview/audio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"jobApplied\":\"BE\"}")
        ).andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("정상 파일이면 201")
    void ok_returns201() throws Exception {
        Mockito.when(interviewService.createInterview(any(), any(), any()))
                .thenReturn(Interview.builder().id(99L).build());

        MockMultipartFile ok = new MockMultipartFile(
                "file", "ok.wav", "audio/wav", "bytes".getBytes()
        );

        mvc.perform(
                multipart("/interview/audio")
                        .file(ok)
                        .param("userId", "1")
                        .param("jobApplied", "BE")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().isCreated());
    }
}