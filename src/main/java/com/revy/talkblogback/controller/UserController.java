package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.AuthContext;
import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.request.ChangePasswordRequest;
import com.revy.talkblogback.pojo.request.UpdateProfileRequest;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.pojo.response.UserProfile;
import com.revy.talkblogback.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/profile")
    @RequireRoles
    public ResponseEntity<ApiResponse<Void>> updateProfile(@RequestBody UpdateProfileRequest request) {
        UserProfile currentUser = AuthContext.get();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.failure("Unauthorized"));
        }
        try {
            userService.updateProfile(currentUser.getUserId(), request.getUsername());
            return ResponseEntity.ok(ApiResponse.success("Profile updated"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireRoles
    public ResponseEntity<ApiResponse<String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        UserProfile currentUser = AuthContext.get();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.failure("Unauthorized"));
        }
        try {
            String avatarUrl = userService.updateAvatar(currentUser.getUserId(), file);
            return ResponseEntity.ok(ApiResponse.success("头像更新成功", avatarUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }

    @PutMapping("/password")
    @RequireRoles
    public ResponseEntity<ApiResponse<Void>> changePassword(@RequestBody ChangePasswordRequest request) {
        UserProfile currentUser = AuthContext.get();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.failure("Unauthorized"));
        }
        try {
            userService.changePassword(currentUser.getUserId(), request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("Password changed"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }
}
