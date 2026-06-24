package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.AuthContext;
import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.Blog;
import com.revy.talkblogback.pojo.Series;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.pojo.response.PageResult;
import com.revy.talkblogback.pojo.response.UserProfile;
import com.revy.talkblogback.service.SeriesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/series")
public class SeriesController {

    private final SeriesService seriesService;

    public SeriesController(SeriesService seriesService) {
        this.seriesService = seriesService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<Series>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResult<Series> result = seriesService.list(page, size);
        return ResponseEntity.ok(ApiResponse.success("success", result));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Series>>> getAll() {
        List<Series> list = seriesService.getAll();
        return ResponseEntity.ok(ApiResponse.success("success", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Series>> detail(@PathVariable Long id) {
        Series series = seriesService.getById(id);
        if (series == null) {
            return ResponseEntity.ok(ApiResponse.failure("Series not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("success", series));
    }

    @PostMapping
    @RequireRoles
    public ResponseEntity<ApiResponse<Series>> create(@RequestBody Series series) {
        UserProfile user = AuthContext.get();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("Unauthorized"));
        }
        try {
            series.setAuthorId(user.getUserId());
            Series created = seriesService.create(series);
            return ResponseEntity.ok(ApiResponse.success("Series created", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequireRoles
    public ResponseEntity<ApiResponse<Series>> update(@PathVariable Long id, @RequestBody Series series) {
        UserProfile user = AuthContext.get();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("Unauthorized"));
        }

        Series existing = seriesService.getById(id);
        if (existing == null) {
            return ResponseEntity.ok(ApiResponse.failure("Series not found"));
        }
        if (!existing.getAuthorId().equals(user.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.failure("No permission"));
        }

        try {
            Series updated = seriesService.update(id, series);
            return ResponseEntity.ok(ApiResponse.success("Series updated", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
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

        Series existing = seriesService.getById(id);
        if (existing == null) {
            return ResponseEntity.ok(ApiResponse.failure("Series not found"));
        }

        boolean isAuthor = existing.getAuthorId().equals(user.getUserId());
        boolean isAdmin = user.getRoles() != null && user.getRoles().stream()
                .anyMatch(r -> r.equalsIgnoreCase("ADMIN") || r.equalsIgnoreCase("SUPER_ADMIN"));

        if (!isAuthor && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.failure("No permission"));
        }

        boolean deleted = seriesService.delete(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Series deleted", null));
        }
        return ResponseEntity.ok(ApiResponse.failure("Delete failed"));
    }

    @GetMapping("/{id}/blogs")
    public ResponseEntity<ApiResponse<PageResult<Blog>>> getBlogs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Blog> result = seriesService.getBlogs(id, page, size);
        return ResponseEntity.ok(ApiResponse.success("success", result));
    }
}
