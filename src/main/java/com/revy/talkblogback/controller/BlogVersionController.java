package com.revy.talkblogback.controller;

import com.revy.talkblogback.pojo.BlogVersion;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.pojo.response.PageResult;
import com.revy.talkblogback.service.BlogVersionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blogs")
public class BlogVersionController {

    private final BlogVersionService blogVersionService;

    public BlogVersionController(BlogVersionService blogVersionService) {
        this.blogVersionService = blogVersionService;
    }

    @GetMapping("/{blogId}/versions")
    public ResponseEntity<ApiResponse<PageResult<BlogVersion>>> getVersions(
            @PathVariable Long blogId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<BlogVersion> result = blogVersionService.getVersions(blogId, page, size);
        return ResponseEntity.ok(ApiResponse.success("success", result));
    }
}
