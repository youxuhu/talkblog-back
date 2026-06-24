package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.Blog;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.pojo.response.PageResult;
import com.revy.talkblogback.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<Blog>>> getRecommendations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Blog> result = recommendationService.getRecommendations(page, size);
        return ResponseEntity.ok(ApiResponse.success("success", result));
    }

    @PostMapping("/behavior")
    @RequireRoles
    public ResponseEntity<ApiResponse<Void>> recordBehavior(@RequestBody Map<String, Object> body) {
        Long blogId = body.get("blogId") != null ? ((Number) body.get("blogId")).longValue() : null;
        String behaviorType = (String) body.get("behaviorType");
        Double score = body.get("score") != null ? ((Number) body.get("score")).doubleValue() : null;

        if (blogId == null || behaviorType == null) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("blogId and behaviorType are required"));
        }

        recommendationService.recordBehavior(blogId, behaviorType, score);
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }
}
