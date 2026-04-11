package com.revy.talkblogback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revy.talkblogback.pojo.request.FaceLoginRequest;
import com.revy.talkblogback.pojo.request.RegisterRequest;
import com.revy.talkblogback.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * LoginController Web 层测试。
 * 目标：验证接口状态码与返回结构是否符合约定。
 */
@WebMvcTest(LoginController.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    /**
     * 注册成功接口测试。
     */
    @Test
    void register_shouldReturnSuccessResponse_whenServiceReturnsTrue() throws Exception {
        RegisterRequest request = new RegisterRequest("demo@test.com", "demo", "123456", "base64-image");
        when(userService.register(any(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /**
     * 登录失败接口测试。
     */
    @Test
    void login_shouldReturnFailureResponse_whenServiceReturnsFalse() throws Exception {
        FaceLoginRequest request = new FaceLoginRequest("demo@test.com", "base64-image");
        when(userService.login("demo@test.com", "base64-image")).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }
}
