package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.request.CreateCommentRequest;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.pojo.response.CommentDetail;
import com.revy.talkblogback.pojo.response.CommentRow;
import com.revy.talkblogback.pojo.response.PageResult;
import com.revy.talkblogback.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @RequireRoles({"USER", "ADMIN", "SUPER_ADMIN"})
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CommentDetail>> createComment(
            @RequestParam("blogId") Long blogId,
            @RequestParam("content") String content,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam(value = "replyToUserId", required = false) Long replyToUserId,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        try {
            CreateCommentRequest request = new CreateCommentRequest();
            request.setBlogId(blogId);
            request.setContent(content);
            request.setParentId(parentId);
            request.setReplyToUserId(replyToUserId);

            CommentDetail comment = commentService.createComment(request, images);
            return ResponseEntity.ok(ApiResponse.success("评论发表成功", comment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<CommentDetail>>> getComments(
            @RequestParam Long blogId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) String status) {
        Short statusValue = null;
        if (status != null && !status.equals("NaN") && !status.isEmpty()) {
            try {
                statusValue = Short.parseShort(status);
            } catch (NumberFormatException e) {
                statusValue = null;
            }
        }
        PageResult<CommentDetail> result = commentService.getCommentsByBlogId(blogId, page, size, parentId, statusValue);
        return ResponseEntity.ok(ApiResponse.success("获取评论成功", result));
    }

    @RequireRoles({"USER", "ADMIN", "SUPER_ADMIN"})
    @PostMapping("/{commentId}/like")
    public ResponseEntity<ApiResponse<Map<String, Object>>> likeComment(@PathVariable Long commentId) {
        try {
            Map<String, Object> result = commentService.likeComment(commentId);
            return ResponseEntity.ok(ApiResponse.success("操作成功", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }

    @RequireRoles({"USER", "ADMIN", "SUPER_ADMIN"})
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.ok(ApiResponse.success("评论已删除", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }

    @RequireRoles({"USER", "ADMIN", "SUPER_ADMIN"})
    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<PageResult<CommentRow>>> getMyComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<CommentRow> result = commentService.getMyComments(page, size);
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }
}
