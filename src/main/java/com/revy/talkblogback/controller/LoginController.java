package com.revy.talkblogback.controller;

import com.revy.talkblogback.pojo.request.FaceLoginRequest;
import com.revy.talkblogback.pojo.request.PasswordLoginRequest;
import com.revy.talkblogback.pojo.request.RegisterRequest;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.pojo.response.AuthResult;
import com.revy.talkblogback.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 控制器层，用于处理用户登录和注册相关的 HTTP 请求。
 */
@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册。
     *
     * @param request 注册请求体（邮箱、用户名、密码）
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequest request) {
        try {
            boolean success = userService.register(request.toUser(), request.getImage());
            return ResponseEntity.ok(success
                    ? ApiResponse.success("Registration successful")
                    : ApiResponse.failure("Email already exists"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(ex.getMessage()));
        }
    }

    /**
     * 人脸登录。
     *
     * @param request 登录请求体（邮箱、人脸图片base64）
     * @return 登录结果
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResult>> login(@RequestBody FaceLoginRequest request) {
        try {
            AuthResult result = userService.loginByFace(request.getEmail(), request.getImage());
            return ResponseEntity.ok(result != null
                    ? ApiResponse.success("Login successful", result)
                    : ApiResponse.failure("Email or face not matched"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(ex.getMessage()));
        }
    }

    /**
     * 密码登录。
     *
     * @param request 登录请求体（邮箱、密码）
     * @return 登录结果
     */
    @PostMapping("/password-login")
    public ResponseEntity<ApiResponse<AuthResult>> passwordLogin(@RequestBody PasswordLoginRequest request) {
        try {
            AuthResult result = userService.loginByPassword(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(result != null
                    ? ApiResponse.success("Login successful", result)
                    : ApiResponse.failure("Email or password not matched"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(ex.getMessage()));
        }
    }
}
