package com.revy.talkblogback.controller;

import com.revy.talkblogback.pojo.request.FaceLoginRequest;
import com.revy.talkblogback.pojo.request.RegisterRequest;
import com.revy.talkblogback.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 控制器层，用于处理用户登录和注册相关的 HTTP 请求。
 */
@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册。
     *
     * @param request 注册请求体（邮箱、用户名、密码）
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        try {
            boolean success = userService.register(request.toUser(), request.getImage());
            Map<String, Object> body = new HashMap<>();
            body.put("success", success);
            body.put("message", success ? "Registration successful" : "Email already exists");
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException ex) {
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
    }

    /**
     * 用户登录。
     *
     * @param request 登录请求体（邮箱、人脸图片base64）
     * @return 登录结果
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody FaceLoginRequest request) {
        try {
            boolean success = userService.login(request.getEmail(), request.getImage());
            Map<String, Object> body = new HashMap<>();
            body.put("success", success);
            body.put("message", success ? "Login successful" : "Email or face not matched");
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException ex) {
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
    }
}
