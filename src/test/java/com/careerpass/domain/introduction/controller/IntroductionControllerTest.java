package com.careerpass.domain.introduction.controller;

import com.careerpass.domain.introduction.dto.IntroductionDtos.CreateRequest;
import com.careerpass.domain.introduction.dto.IntroductionDtos.Response;
import com.careerpass.domain.introduction.exception.IntroductionNotFoundException;
import com.careerpass.domain.introduction.service.IntroductionService;
import com.careerpass.global.error.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = IntroductionController.class)
@AutoConfigureMockMvc(addFilters = false)   // ★ 보안 필터(인증/CSRF) 비활성화
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class IntroductionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    IntroductionService introductionService;

    @Test
    @DisplayName("POST /api/introductions - 정상 생성시 201")
    void create_ok_201() throws Exception {
        CreateRequest req = new CreateRequest(1L, "백엔드", "내용", LocalDateTime.now());
        Response res = new Response(10L, 1L, "백엔드", "내용", req.submissionTime());
        Mockito.when(introductionService.create(any())).thenReturn(res);

        mockMvc.perform(post("/api/introductions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    @DisplayName("GET /api/introductions/{id} - 존재하지 않으면 404")
    void get_notFound_404() throws Exception {
        long id = 999L;
        Mockito.when(introductionService.get(id))
                .thenThrow(new IntroductionNotFoundException(id));

        mockMvc.perform(get("/api/introductions/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("I001"));
    }

    @Test
    @DisplayName("GET /api/introductions?userId= - 목록 조회 200")
    void list_ok_200() throws Exception {
        List<Response> list = List.of(
                new Response(1L, 1L, "백엔드", "내용", LocalDateTime.now())
        );
        Mockito.when(introductionService.listByUser(1L)).thenReturn(list);

        mockMvc.perform(get("/api/introductions").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }
}