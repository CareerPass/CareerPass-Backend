package com.careerpass.domain.feedback.controller;

import com.careerpass.domain.feedback.dto.FeedbackDtos;
import com.careerpass.domain.feedback.entity.FeedbackType;
import com.careerpass.domain.feedback.exception.FeedbackNotFoundException;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FeedbackController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class FeedbackControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    FeedbackService feedbackService;

    @Test
    @DisplayName("GET /api/feedback/{id} - 정상 id는 200 응답")
    void get_ok_200() throws Exception {
        var response = new FeedbackDtos.Response(
                1L,
                FeedbackType.INTRODUCTION, // enum 값 (예: INTRODUCTION, INTERVIEW 중 하나)
                90L,
                "좋은 자기소개입니다.",
                "각 항목별로 명확히 작성되어 있습니다.",
                1L,
                null
        );
        when(feedbackService.get(1L)).thenReturn(response);

        mockMvc.perform(get("/api/feedback/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.feedbackType").value("INTRODUCTION"))
                .andExpect(jsonPath("$.totalScore").value(90));
    }

    @Test
    @DisplayName("GET /api/feedback/{id} - 존재하지 않으면 404")
    void get_notFound_404() throws Exception {
        when(feedbackService.get(anyLong()))
                .thenThrow(new FeedbackNotFoundException(999L));

        mockMvc.perform(get("/api/feedback/{id}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("F001"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/feedback/{id} - 0 또는 음수 id면 400")
    void get_invalidId_400() throws Exception {
        mockMvc.perform(get("/api/feedback/{id}", 0L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(get("/api/feedback/{id}", -1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))
                .andExpect(jsonPath("$.status").value(400));
    }
}