package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.AuthContext;
import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.Blog;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.pojo.response.PageResult;
import com.revy.talkblogback.pojo.response.UserProfile;
import com.revy.talkblogback.service.BlogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<Blog>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long seriesId,
            @RequestParam(required = false) String category) {
        PageResult<Blog> result = blogService.list(page, size, keyword, seriesId, category);
        return ResponseEntity.ok(ApiResponse.success("success", result));
    }

    @GetMapping("/my")
    @RequireRoles
    public ResponseEntity<ApiResponse<PageResult<Blog>>> myBlogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Short status) {
        UserProfile user = AuthContext.get();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("Unauthorized"));
        }
        PageResult<Blog> result = blogService.listMyBlogs(user.getUserId(), page, size, status);
        return ResponseEntity.ok(ApiResponse.success("success", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Blog>> detail(@PathVariable Long id) {
        Blog blog = blogService.getByIdAndIncrementViewCount(id);
        if (blog == null) {
            return ResponseEntity.ok(ApiResponse.failure("Blog not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("success", blog));
    }

    @PostMapping
    @RequireRoles
    public ResponseEntity<ApiResponse<Blog>> create(@RequestBody Blog blog) {
        UserProfile user = AuthContext.get();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("Unauthorized"));
        }

        try {
            blog.setAuthorId(user.getUserId());
            Blog created = blogService.create(blog);
            return ResponseEntity.ok(ApiResponse.success("Blog created", created));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequireRoles
    public ResponseEntity<ApiResponse<Blog>> update(@PathVariable Long id, @RequestBody Blog blog) {
        UserProfile user = AuthContext.get();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("Unauthorized"));
        }

        Blog existing = blogService.getById(id);
        if (existing == null) {
            return ResponseEntity.ok(ApiResponse.failure("Blog not found"));
        }

        if (!existing.getAuthorId().equals(user.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.failure("No permission"));
        }

        try {
            Blog updated = blogService.update(id, blog);
            return ResponseEntity.ok(ApiResponse.success("Blog updated", updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @RequireRoles
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        UserProfile user = AuthContext.get();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("Unauthorized"));
        }

        Blog existing = blogService.getById(id);
        if (existing == null) {
            return ResponseEntity.ok(ApiResponse.failure("Blog not found"));
        }

        boolean isAuthor = existing.getAuthorId().equals(user.getUserId());
        boolean isAdmin = user.getRoles() != null && user.getRoles().stream()
                .anyMatch(r -> r.equalsIgnoreCase("ADMIN") || r.equalsIgnoreCase("SUPER_ADMIN"));

        if (!isAuthor && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.failure("No permission"));
        }

        boolean deleted = blogService.delete(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Blog deleted", null));
        }
        return ResponseEntity.ok(ApiResponse.failure("Delete failed"));
    }
}