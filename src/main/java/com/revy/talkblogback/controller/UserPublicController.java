package com.revy.talkblogback.controller;

import com.revy.talkblogback.mapper.LoginMapper;
import com.revy.talkblogback.pojo.Blog;
import com.revy.talkblogback.pojo.Series;
import com.revy.talkblogback.pojo.User;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.pojo.response.CommentRow;
import com.revy.talkblogback.pojo.response.PageResult;
import com.revy.talkblogback.service.BlogService;
import com.revy.talkblogback.service.CommentService;
import com.revy.talkblogback.service.FollowService;
import com.revy.talkblogback.service.SeriesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserPublicController {

    private final LoginMapper loginMapper;
    private final BlogService blogService;
    private final CommentService commentService;
    private final FollowService followService;
    private final SeriesService seriesService;

    public UserPublicController(LoginMapper loginMapper, BlogService blogService,
                                CommentService commentService, FollowService followService,
                                SeriesService seriesService) {
        this.loginMapper = loginMapper;
        this.blogService = blogService;
        this.commentService = commentService;
        this.followService = followService;
        this.seriesService = seriesService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> profile(@PathVariable Long userId) {
        User user = loginMapper.findUserById(userId);
        if (user == null || user.getStatus() != 1) {
            return ResponseEntity.notFound().build();
        }
        long blogCount = blogService.countByAuthorId(userId, Blog.STATUS_PUBLISHED);
        long commentCount = commentService.countByUserId(userId);
        long followerCount = followService.countFollowers(userId);
        long followeeCount = followService.countFollowees(userId);
        long seriesCount = seriesService.countByAuthorId(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("username", user.getUsername());
        data.put("avatarUrl", user.getAvatarUrl());
        data.put("blogCount", blogCount);
        data.put("commentCount", commentCount);
        data.put("followerCount", followerCount);
        data.put("followeeCount", followeeCount);
        data.put("seriesCount", seriesCount);
        data.put("createdAt", user.getCreatedAt());
        return ResponseEntity.ok(ApiResponse.success("success", data));
    }

    @GetMapping("/{userId}/blogs")
    public ResponseEntity<ApiResponse<PageResult<Blog>>> blogs(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Blog> result = blogService.listMyBlogs(userId, page, size, Blog.STATUS_PUBLISHED);
        return ResponseEntity.ok(ApiResponse.success("success", result));
    }

    @GetMapping("/{userId}/comments")
    public ResponseEntity<ApiResponse<PageResult<CommentRow>>> comments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<CommentRow> result = commentService.getUserComments(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success("success", result));
    }

    @GetMapping("/{userId}/series")
    public ResponseEntity<ApiResponse<PageResult<Series>>> series(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Series> result = seriesService.getUserSeries(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success("success", result));
    }
}
