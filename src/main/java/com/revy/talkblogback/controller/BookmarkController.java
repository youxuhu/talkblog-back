package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.AuthContext;
import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.Blog;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.pojo.response.PageResult;
import com.revy.talkblogback.pojo.response.UserProfile;
import com.revy.talkblogback.service.BookmarkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping("/{blogId}")
    @RequireRoles
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggle(@PathVariable Long blogId) {
        UserProfile user = AuthContext.get();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.failure("Unauthorized"));
        }
        boolean bookmarked = bookmarkService.toggle(user.getUserId(), blogId);
        return ResponseEntity.ok(ApiResponse.success(bookmarked ? "Bookmarked" : "Unbookmarked",
                Map.of("bookmarked", bookmarked)));
    }

    @GetMapping("/{blogId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status(@PathVariable Long blogId) {
        UserProfile user = AuthContext.get();
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.success("success", Map.of("bookmarked", false)));
        }
        boolean bookmarked = bookmarkService.isBookmarked(user.getUserId(), blogId);
        return ResponseEntity.ok(ApiResponse.success("success", Map.of("bookmarked", bookmarked)));
    }

    @GetMapping
    @RequireRoles
    public ResponseEntity<ApiResponse<PageResult<Blog>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserProfile user = AuthContext.get();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.failure("Unauthorized"));
        }
        PageResult<Blog> result = bookmarkService.getBookmarkedBlogs(user.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.success("success", result));
    }
}
