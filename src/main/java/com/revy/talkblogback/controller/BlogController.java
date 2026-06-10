package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.AuthContext;
import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.Blog;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.pojo.response.PageResult;
import com.revy.talkblogback.pojo.response.UserProfile;
import com.revy.talkblogback.service.BlogService;
import com.revy.talkblogback.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    private final BlogService blogService;
    private final FileService fileService;

    public BlogController(BlogService blogService, FileService fileService) {
        this.blogService = blogService;
        this.fileService = fileService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<Blog>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String sortBy) {
        Long currentUserId = getCurrentUserIdIfAuthenticated();
        PageResult<Blog> result = blogService.list(page, size, keyword, categoryId, tagId, currentUserId, sortBy);
        return ResponseEntity.ok(ApiResponse.success("success", result));
    }

    @GetMapping("/my")
    @RequireRoles
    public ResponseEntity<ApiResponse<PageResult<Blog>>> myBlogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserProfile user = AuthContext.get();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("Unauthorized"));
        }
        PageResult<Blog> result = blogService.listMyBlogs(user.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.success("success", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Blog>> detail(@PathVariable Long id) {
        Long currentUserId = getCurrentUserIdIfAuthenticated();
        Blog blog = blogService.getById(id, currentUserId);
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

        if (!existing.getAuthorId().equals(user.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.failure("No permission"));
        }

        boolean deleted = blogService.delete(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Blog deleted", null));
        }
        return ResponseEntity.ok(ApiResponse.failure("Delete failed"));
    }

    @PostMapping("/upload-image")
    @RequireRoles
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure("请选择要上传的图片"));
        }
        try {
            String url = fileService.uploadBlogImage(file);
            return ResponseEntity.ok(ApiResponse.success("success", url));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("图片上传失败: " + ex.getMessage()));
        }
    }

    @DeleteMapping("/upload-image")
    @RequireRoles
    public ResponseEntity<ApiResponse<Void>> deleteImage(@RequestParam("url") String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure("图片地址不能为空"));
        }
        try {
            fileService.deleteBlogImage(imageUrl);
            return ResponseEntity.ok(ApiResponse.success("图片已删除"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("删除图片失败: " + ex.getMessage()));
        }
    }

    @PostMapping("/{id}/like")
    @RequireRoles
    public ResponseEntity<ApiResponse<Object>> like(@PathVariable Long id) {
        try {
            Object result = blogService.likeBlog(id);
            return ResponseEntity.ok(ApiResponse.success("success", result));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ex.getMessage()));
        }
    }

    @PostMapping("/{id}/favorite")
    @RequireRoles
    public ResponseEntity<ApiResponse<Object>> favorite(@PathVariable Long id) {
        try {
            Object result = blogService.favoriteBlog(id);
            return ResponseEntity.ok(ApiResponse.success("success", result));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(ex.getMessage()));
        }
    }

    @GetMapping("/favorites")
    @RequireRoles
    public ResponseEntity<ApiResponse<PageResult<Blog>>> favorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Blog> result = blogService.getFavorites(page, size);
        return ResponseEntity.ok(ApiResponse.success("success", result));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<ApiResponse<Void>> recordView(@PathVariable Long id, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        blogService.recordView(id, ip);
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<Blog>>> popular(@RequestParam(defaultValue = "10") int limit) {
        List<Blog> list = blogService.getPopular(limit);
        return ResponseEntity.ok(ApiResponse.success("success", list));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<Blog>>> trending(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {
        List<Blog> list = blogService.getTrending(days, limit);
        return ResponseEntity.ok(ApiResponse.success("success", list));
    }

    private Long getCurrentUserIdIfAuthenticated() {
        try {
            UserProfile user = AuthContext.get();
            return user != null ? user.getUserId() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
