package com.careerpass.domain.feedback.controller;

import com.careerpass.domain.feedback.dto.FeedbackDtos;
import com.careerpass.domain.feedback.entity.FeedbackType;
import com.careerpass.domain.feedback.service.FeedbackService;
import com.careerpass.global.error.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FeedbackController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class FeedbackControllerCreateTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean FeedbackService feedbackService;

    @Test
    @DisplayName("POST /api/feedback - 정상 생성 시 201")
    void create_ok_201() throws Exception {
        var req = new FeedbackDtos.CreateRequest(
                FeedbackType.INTRODUCTION, // enum 값
                95L,
                "전체적으로 훌륭합니다.",
                "문항별 코멘트",
                10L,  // introductionId
                null  // interviewId
        );

        var resp = new FeedbackDtos.Response(
                1L,
                FeedbackType.INTRODUCTION,
                95L,
                "전체적으로 훌륭합니다.",
                "문항별 코멘트",
                10L,
                null
        );

        when(feedbackService.create(any(FeedbackDtos.CreateRequest.class))).thenReturn(resp);

        mockMvc.perform(
                        post("/api/feedback")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.feedbackType").value("INTRODUCTION"))
                .andExpect(jsonPath("$.totalScore").value(95));
    }

    @Test
    @DisplayName("POST /api/feedback - 필수 필드 누락/빈값이면 400")
    void create_invalidBody_400() throws Exception {
        // feedbackType=null, totalScore=null, feedbackText="", sectionFeedback=""
        var badReq = new FeedbackDtos.CreateRequest(
                null,
                null,
                "",
                "",
                null,
                null
        );

        mockMvc.perform(
                        post("/api/feedback")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(badReq))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))   // INVALID_INPUT_VALUE
                .andExpect(jsonPath("$.status").value(400));
    }
}