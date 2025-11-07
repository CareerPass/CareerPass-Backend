package com.careerpass.domain.feedback.controller;

import com.careerpass.domain.feedback.dto.FeedbackDtos;
import com.careerpass.domain.feedback.entity.FeedbackType;
import com.careerpass.domain.feedback.service.FeedbackService;
import com.careerpass.global.error.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FeedbackController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class FeedbackControllerListTest {

    @Autowired MockMvc mockMvc;
    @MockBean FeedbackService feedbackService;

    @Test
    @DisplayName("GET /api/feedback/introduction/{id} - 정상 200")
    void listByIntroduction_ok_200() throws Exception {
        var list = List.of(
                new FeedbackDtos.Response(1L, FeedbackType.INTRODUCTION, 90L, "좋음", "섹션1", 10L, null),
                new FeedbackDtos.Response(2L, FeedbackType.INTRODUCTION, 85L, "보통", "섹션2", 10L, null)
        );
        when(feedbackService.listByIntroduction(10L)).thenReturn(list);

        mockMvc.perform(
                        get("/api/feedback/introduction/{introductionId}", 10L)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].feedbackType").value("INTRODUCTION"));
    }

    @Test
    @DisplayName("GET /api/feedback/introduction/{id} - 0이면 400")
    void listByIntroduction_invalidId_400() throws Exception {
        mockMvc.perform(
                        get("/api/feedback/introduction/{introductionId}", 0L)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))  // INVALID_INPUT_VALUE
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/feedback/interview/{id} - 정상 200")
    void listByInterview_ok_200() throws Exception {
        var list = List.of(
                new FeedbackDtos.Response(3L, FeedbackType.INTERVIEW, 77L, "개선 필요", "파트A", null, 20L)
        );
        when(feedbackService.listByInterview(20L)).thenReturn(list);

        mockMvc.perform(
                        get("/api/feedback/interview/{interviewId}", 20L)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].feedbackType").value("INTERVIEW"));
    }

    @Test
    @DisplayName("GET /api/feedback/interview/{id} - 0이면 400")
    void listByInterview_invalidId_400() throws Exception {
        mockMvc.perform(
                        get("/api/feedback/interview/{interviewId}", 0L)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))
                .andExpect(jsonPath("$.status").value(400));
    }
}