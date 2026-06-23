package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.AuthContext;
import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.pojo.response.UserProfile;
import com.revy.talkblogback.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{userId}")
    @RequireRoles
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggle(@PathVariable Long userId) {
        UserProfile user = AuthContext.get();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.failure("Unauthorized"));
        }
        boolean following = followService.toggle(user.getUserId(), userId);
        long followerCount = followService.countFollowers(userId);
        return ResponseEntity.ok(ApiResponse.success(following ? "Followed" : "Unfollowed",
                Map.of("following", following, "followerCount", followerCount)));
    }

    @GetMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status(@PathVariable Long userId) {
        UserProfile user = AuthContext.get();
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.success("success", Map.of("following", false)));
        }
        boolean following = followService.isFollowing(user.getUserId(), userId);
        return ResponseEntity.ok(ApiResponse.success("success", Map.of("following", following)));
    }

    @GetMapping("/me/followees")
    @RequireRoles
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> myFollowees() {
        UserProfile user = AuthContext.get();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.failure("Unauthorized"));
        }
        List<Map<String, Object>> list = followService.getFollowees(user.getUserId(), 1, 1000);
        return ResponseEntity.ok(ApiResponse.success("success", list));
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<ApiResponse<?>> followers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Map<String, Object>> list = followService.getFollowers(userId, page, size);
        long total = followService.countFollowers(userId);
        return ResponseEntity.ok(ApiResponse.success("success", Map.of(
                "list", list, "total", total, "page", page, "size", size)));
    }

    @GetMapping("/{userId}/followees")
    public ResponseEntity<ApiResponse<?>> followees(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Map<String, Object>> list = followService.getFollowees(userId, page, size);
        long total = followService.countFollowees(userId);
        return ResponseEntity.ok(ApiResponse.success("success", Map.of(
                "list", list, "total", total, "page", page, "size", size)));
    }
}
