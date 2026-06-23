package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.AuthContext;
import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.Blog;
import com.revy.talkblogback.pojo.request.ChangePasswordRequest;
import com.revy.talkblogback.pojo.request.UpdateProfileRequest;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.pojo.response.CommentRow;
import com.revy.talkblogback.pojo.response.PageResult;
import com.revy.talkblogback.pojo.response.UserProfile;
import com.revy.talkblogback.service.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final BlogService blogService;
    private final CommentService commentService;
    private final FollowService followService;
    private final SeriesService seriesService;
    private final BookmarkService bookmarkService;

    public UserController(UserService userService, BlogService blogService,
                          CommentService commentService, FollowService followService,
                          SeriesService seriesService, BookmarkService bookmarkService) {
        this.userService = userService;
        this.blogService = blogService;
        this.commentService = commentService;
        this.followService = followService;
        this.seriesService = seriesService;
        this.bookmarkService = bookmarkService;
    }

    @GetMapping("/me")
    @RequireRoles
    public ResponseEntity<ApiResponse<UserProfile>> me() {
        UserProfile currentUser = AuthContext.get();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.failure("Unauthorized"));
        }
        return ResponseEntity.ok(ApiResponse.success("success", currentUser));
    }

    @GetMapping("/home")
    @RequireRoles
    public ResponseEntity<ApiResponse<Map<String, Object>>> home() {
        UserProfile currentUser = AuthContext.get();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.failure("Unauthorized"));
        }
        Long userId = currentUser.getUserId();
        long blogCount = blogService.countByAuthorId(userId, Blog.STATUS_PUBLISHED);
        long commentCount = commentService.countByUserId(userId);
        long followerCount = followService.countFollowers(userId);
        long followeeCount = followService.countFollowees(userId);
        long seriesCount = seriesService.countByAuthorId(userId);
        long bookmarkCount = bookmarkService.countByUserId(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", currentUser.getUserId());
        data.put("username", currentUser.getUsername());
        data.put("email", currentUser.getEmail());
        data.put("avatarUrl", currentUser.getAvatarUrl());
        data.put("phone", currentUser.getPhone());
        data.put("status", currentUser.getStatus());
        data.put("loginType", currentUser.getLoginType());
        data.put("roles", currentUser.getRoles());
        data.put("lastLoginTime", currentUser.getLastLoginTime());
        data.put("blogCount", blogCount);
        data.put("commentCount", commentCount);
        data.put("followerCount", followerCount);
        data.put("followeeCount", followeeCount);
        data.put("seriesCount", seriesCount);
        data.put("bookmarkCount", bookmarkCount);
        return ResponseEntity.ok(ApiResponse.success("success", data));
    }

    @GetMapping("/center")
    @RequireRoles
    public ResponseEntity<ApiResponse<Map<String, Object>>> center(
            @RequestParam(defaultValue = "1") int bookmarkPage,
            @RequestParam(defaultValue = "10") int bookmarkSize,
            @RequestParam(defaultValue = "1") int commentPage,
            @RequestParam(defaultValue = "10") int commentSize) {
        UserProfile currentUser = AuthContext.get();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.failure("Unauthorized"));
        }
        Long userId = currentUser.getUserId();

        PageResult<Blog> bookmarks = bookmarkService.getBookmarkedBlogs(userId, bookmarkPage, bookmarkSize);
        PageResult<CommentRow> comments = commentService.getMyComments(commentPage, commentSize);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", currentUser.getUserId());
        data.put("username", currentUser.getUsername());
        data.put("email", currentUser.getEmail());
        data.put("avatarUrl", currentUser.getAvatarUrl());
        data.put("phone", currentUser.getPhone());
        data.put("bookmarks", bookmarks);
        data.put("comments", comments);
        return ResponseEntity.ok(ApiResponse.success("success", data));
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
