package com.careerpass.domain.user.controller;

import com.careerpass.domain.user.dto.CreateUserRequest;
import com.careerpass.domain.user.dto.ProfileResponse;
import com.careerpass.domain.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
@WithMockUser(username = "testuser")
class UserControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean UserService userService;

    @Test
    @DisplayName("POST /api/users - 정상 생성 (201)")
    void create_201() throws Exception {
        CreateUserRequest req = new CreateUserRequest("윤서", "yun@mju.ac.kr", "컴공", "백엔드");
        ProfileResponse res = new ProfileResponse(1L, "윤서", "yun@mju.ac.kr", "컴공", "백엔드");

        BDDMockito.given(userService.create(req)).willReturn(res);

        mvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("yun@mju.ac.kr")));
    }

    @Test
    @DisplayName("POST /api/users - 이메일 누락 시 400")
    void create_invalid_400() throws Exception {
        CreateUserRequest req = new CreateUserRequest("윤도", "", "컴공", "백엔드");

        mvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/users/{id} - id가 0이면 400")
    void get_invalid_id_400() throws Exception {
        mvc.perform(get("/api/users/{id}", 0))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/users/{id} - 정상 조회 (200)")
    void get_200() throws Exception {
        ProfileResponse res = new ProfileResponse(1L, "윤도", "do@mju.ac.kr", "컴공", "백엔드");
        BDDMockito.given(userService.getById(1L)).willReturn(res);

        mvc.perform(get("/api/users/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nickname", is("윤도")));
    }
}